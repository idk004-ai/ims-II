package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface JwtService {
    String createAccessToken(User user);

    String createRefreshToken(User user, boolean isRememberMe);

    void setAccessTokenCookie(HttpServletResponse response, String accessToken);

    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    void removeRefreshTokenCookie(HttpServletResponse response);

    void removeAccessTokenCookie(HttpServletResponse response);

    String getRefreshTokenFromCookie(HttpServletRequest request);

    String getEmailFromToken(String token);

    String getAccessTokenFromCookie(HttpServletRequest request);

    boolean validateRefreshToken(String refreshToken, HttpServletRequest request);

    boolean validateAccessToken(String accessToken);

    void removeByEmail(String email);

    void revokeToken(String token, String reason);

    void revokeAllTokens(String email, String reason);

}
