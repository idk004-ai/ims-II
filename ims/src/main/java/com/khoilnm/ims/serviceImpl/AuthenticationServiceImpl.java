package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.exceptions.TokenRevocationException;
import com.khoilnm.ims.service.AuthenticationService;
import com.khoilnm.ims.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRevocationServiceImpl tokenRevocationService;

    public AuthenticationServiceImpl(@Lazy AuthenticationManager authenticationManager, JwtService jwtService, TokenRevocationServiceImpl tokenRevocationService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
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
            tokenRevocationService.revokeToken(refreshToken, TokenRevocationServiceImpl.RevokeReason.USER_LOGOUT);
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
     * <h1>Revoke all tokens and force logout user when password is changed</h1>
     *
     * @param email       String
     * @param newPassword String
     * @throws TokenRevocationException Exception
     */
    public void changePassword(String email, String newPassword) throws TokenRevocationException {
        // TODO: Implement password change


        // Revoke all tokens
        tokenRevocationService.revokeAllUserTokens(email, TokenRevocationServiceImpl.RevokeReason.PASSWORD_CHANGE);
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