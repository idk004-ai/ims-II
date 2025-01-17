package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.Base32Utils;
import com.khoilnm.ims.dto.UserDTO;
import com.khoilnm.ims.exceptions.TokenRevocationException;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.AuthenticationService;
import com.khoilnm.ims.service.JwtService;
import com.khoilnm.ims.service.UserReadService;
import com.khoilnm.ims.service.UserWriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRevocationServiceImpl tokenRevocationService;
    private final UserWriteService<UserDTO, User> userWriteService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    public AuthenticationServiceImpl(
            @Lazy AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenRevocationServiceImpl tokenRevocationService,
            UserWriteService<UserDTO, User> userWriteService,
            UserReadService userReadService,
            MessageSource messageSource) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.userWriteService = userWriteService;
        this.userReadService = userReadService;
        this.messageSource = messageSource;
    }

    /**
     * @param email    String
     * @param password String
     * @return Authentication
     */
    @Override
    public Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    /**
     * @param user User
     * @return String
     */
    @Override
    public String createPasswordResetUrl(User user) {
        String resetToken = Base32Utils.generateResetToken(user.getId(), user.getEmail());
        Date expiryDate = new Date(System.currentTimeMillis() + Base32Utils.RESET_TOKEN_EXPIRATION * 1000);
        userWriteService.updateResetPasswordToken(
                resetToken,
                expiryDate,
                user.getEmail()
        );
        String resetUrl = "http://localhost:8080/api/v1/auth/reset-password/" + resetToken;
        log.info("Created password reset url: {}", resetUrl);
        return resetUrl;
    }

    /**
     * @param token String
     * @return boolean
     */
    @Override
    public boolean validateResetToken(String token) {
        User user = userReadService.findByResetToken(token);
        Base32Utils.TokenInfo decodedToken = Base32Utils.decodeResetToken(token);

        if (!Base32Utils.isValidToken(token)) {
            log.warn("Invalid token length");
            return false;
        }

        if (!Objects.equals(decodedToken.getUserId(), user.getId())) {
            log.warn("Token does not match user id");
            return false;
        }

        if (user.isUsedResetPasswordToken()) {
            log.warn("Token has already been used");
            return false;
        }

        Date now = new Date();
        Date tokenCreationTime = decodedToken.getTimestamp();
        long expirationTimeMillis = tokenCreationTime.getTime() + (Base32Utils.RESET_TOKEN_EXPIRATION * 1000);

        if (now.getTime() > expirationTimeMillis) {
            log.warn("Token has expired");
            return false;
        }

        return true;
    }

    /**
     * @param email String
     */
    @Override
    public void markTokenAsUsed(String email) {
        User user = userReadService.findByEmail(email).orElseThrow(() -> {
            String error = messageSource.getMessage("ME000", null, Locale.getDefault());
            return new NoSuchElementException(error);
        });
        user.setUsedResetPasswordToken(true);
        userWriteService.update(user);
    }

    /**
     * @param email String
     * @param newPassword String
     */
    @Override
    public void changePassword(String email, String newPassword) throws TokenRevocationException {
        User user = userReadService.findByEmail(email).orElseThrow(() -> {
            String error = messageSource.getMessage("ME000", null, Locale.getDefault());
            return new NoSuchElementException(error);
        });
        user.setPassword(newPassword);
        userWriteService.update(user);

        tokenRevocationService.revokeAllUserTokens(user.getEmail(), TokenRevocationServiceImpl.RevokeReason.PASSWORD_CHANGE);
    }

    /**
     * @param request        HttpServletRequest
     * @param response       HttpServletResponse
     * @param authentication Authentication
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            if (authentication == null) return;
            final String refreshToken = jwtService.getRefreshTokenFromCookie(request);
            String email = jwtService.getEmailFromToken(refreshToken);
            jwtService.removeByEmail(email);
            tokenRevocationService.revokeToken(email, refreshToken, TokenRevocationServiceImpl.RevokeReason.USER_LOGOUT);
        } catch (Exception e) {
            log.error("Error while logging out: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (TokenRevocationException e) {
            log.error("Error while revoking token to logout: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {

            jwtService.removeAccessTokenCookie(response);

            // Clear refresh token cookie
            jwtService.removeRefreshTokenCookie(response);

            // Clear security context
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * <h1>Revoke all tokens and force logout user when account is disabled</h1>
     *
     * @param email String
     * @throws TokenRevocationException Exception
     */
    public void disableAccount(String email) throws TokenRevocationException {
        // TODO: Implement account disable

        // Revoke all tokens
        tokenRevocationService.revokeAllUserTokens(email, TokenRevocationServiceImpl.RevokeReason.ACCOUNT_DISABLED);
    }

    /**
     * <h1>Revoke all tokens when user forcing to log out on all devices</h1>
     *
     * @param email String
     * @throws TokenRevocationException Exception
     */
    public void forceLogout(String email) throws TokenRevocationException {
        // Revoke all tokens
        tokenRevocationService.revokeAllUserTokens(email, TokenRevocationServiceImpl.RevokeReason.FORCE_LOGOUT);
    }
}