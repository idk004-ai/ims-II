package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.exceptions.TokenRevocationException;
import com.khoilnm.ims.model.RefreshToken;
import com.khoilnm.ims.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@Slf4j
public class TokenRevocationServiceImpl {

    private final RefreshTokenRepository refreshTokenRepository;


    public TokenRevocationServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Các trường hợp revoke token
     */
    public enum RevokeReason {
        USER_LOGOUT("User initiated logout"),
        PASSWORD_CHANGE("Password changed"),
        SECURITY_BREACH("Security breach detected"),
        SUSPICIOUS_ACTIVITY("Suspicious activity detected"),
        ADMIN_ACTION("Administrative action"),
        TOKEN_ROTATION("Token rotation"),
        MAX_SESSIONS_EXCEEDED("Maximum sessions exceeded"),
        ACCOUNT_DISABLED("Account disabled"),
        FORCE_LOGOUT("Forced logout from all devices");

        private final String description;

        RevokeReason(String description) {
            this.description = description;
        }
    }

    /**
     * Revoke single token
     */
    @Transactional
    public void revokeToken(String email, String token, RevokeReason reason) throws TokenRevocationException {
        try {
            RefreshToken refreshToken = refreshTokenRepository
                    .findByToken(token)
                    .orElseThrow(() -> new NoSuchElementException("Token not found"));

            performTokenRevocation(refreshToken, reason, email);

        } catch (Exception e) {
            log.error("Error revoking token", e);
            throw new TokenRevocationException("Failed to revoke token", e);
        }
    }

    /**
     * Revoke all tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(String email, RevokeReason reason) throws TokenRevocationException {
        try {
            List<RefreshToken> activeTokens = refreshTokenRepository
                    .findAllByEmailAndRevokeFalseOrderByCreatedDateDesc(email, false);

            activeTokens.forEach(token -> performTokenRevocation(token, reason, email));

        } catch (Exception e) {
            log.error("Error revoking all tokens for user: {}", email, e);
            throw new TokenRevocationException(
                    "Failed to revoke all tokens for user", e);
        }
    }


    /**
     * Thực hiện revoke token
     */
    private void performTokenRevocation(RefreshToken token, RevokeReason reason, String email) {
        token.setRevoke(true);
        token.setRevokedAt(new Date());
        token.setRevokeReason(reason.description);

        refreshTokenRepository.save(token);

        log.info("Token revoked - User: {}, Time: {}, By: {}, Reason: {}",
                token.getEmail(), new Date(), email, reason.description);
    }


    /**
     * Enforce maximum active sessions per user
     */
    @Transactional
    public void enforceMaxActiveSessions(String email) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findAllByEmailAndRevokeFalseOrderByCreatedDateDesc(email, true);

        int MAX_ACTIVE_SESSIONS = 5;
        if (activeTokens.size() > MAX_ACTIVE_SESSIONS) {
            log.info("User {} has {} active sessions, maximum allowed is {}",
                    email, activeTokens.size(), MAX_ACTIVE_SESSIONS);
            // Revoke oldest tokens exceeding the limit
            activeTokens.stream()
                    .skip(MAX_ACTIVE_SESSIONS)
                    .forEach(token ->
                            performTokenRevocation(token, RevokeReason.MAX_SESSIONS_EXCEEDED, email)
                    );
        }
    }

    /**
     * Cleanup expired and revoked tokens
     */
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void cleanupTokens() {
        try {
            // Delete tokens revoked more than 30 days ago
            Date thirtyDaysAgo = Date.from(
                    Instant.now().minus(30, ChronoUnit.DAYS));

            int deletedRevokedTokens = refreshTokenRepository
                    .deleteByRevokeTrueAndRevokedAtBefore(thirtyDaysAgo);

            // Delete expired tokens
            int deletedExpiredTokens = refreshTokenRepository
                    .deleteByExpiryDateBefore(new Date());

            log.info("Token cleanup completed. Deleted {} revoked tokens and {} expired tokens",
                    deletedRevokedTokens, deletedExpiredTokens);

        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }
}
