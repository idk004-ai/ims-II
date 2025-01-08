package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface JwtService {
    String createAccessToken(User user);
    String createRefreshToken(User user);

    void setAccessTokenCookie(HttpServletResponse response, String accessToken);

    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    String getRefreshTokenFromCookie(HttpServletRequest request);

    String getEmailFromRefreshToken(String refreshToken);

    boolean validateRefreshToken(String refreshToken);
}
