package com.khoilnm.ims.common;

import com.khoilnm.ims.exceptions.TokenCreationException;
import com.khoilnm.ims.exceptions.TokenDecodeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;

@Slf4j
public class Base32Utils {
    private static final Base32 base32 = new Base32();
    private static final SecureRandom secureRandom = new SecureRandom();
    public static final long RESET_TOKEN_EXPIRATION = 60 * 60 * 24;
    public static final int DEFAULT_TOKEN_LENGTH = 32;

    /**
     * <h3>Token decoded information</h3>
     */
    @Getter
    @AllArgsConstructor
    public static class TokenInfo {
        private final Integer userId;
        private final Date timestamp;
    }

    /**
     * <h3>Create a reset password token with personal identifiers: email, userId</h3>
     *  <p>Token format: {userId}:{timeStamp}:{random}</p>
     *
     * @param userId Integer
     * @param email String
     * @return String
     */
    public static String generateResetToken(Integer userId, String email) throws TokenCreationException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(32);
            buffer.putInt(userId);
            buffer.putLong(System.currentTimeMillis());
            byte[] randomBytes = new byte[16];
            secureRandom.nextBytes(randomBytes);
            buffer.put(randomBytes);

            // encode the token
            return base32.encodeToString(buffer.array());
        } catch (Exception e) {
            throw new TokenCreationException("Failed to create reset token", e);
        }
    }

    /**
     * <h3>Decode the reset password token</h3>
     * @param token String
     * @return TokenInfo
     */
    public static TokenInfo decodeResetToken(String token) {
        try {
            byte[] decoded = base32.decode(token);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            Integer userId = buffer.getInt();
            long timestamp = buffer.getLong();

            return new TokenInfo(userId, new Date(timestamp));
        } catch (Exception e) {
            throw new TokenDecodeException("Failed to decode reset token", e);
        }
    }

    public static boolean isValidToken(String token) {
        try {
            byte[] decoded = base32.decode(token);
            return decoded.length == DEFAULT_TOKEN_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }
}
