package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.AuthenticationService;
import com.khoilnm.ims.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authentication_manager;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(AuthenticationManager authentication_manager, JwtService jwtService) {
        this.authentication_manager = authentication_manager;
        this.jwtService = jwtService;
    }

    /**
     * @param email
     * @param password
     * @return Authentication
     */
    @Override
    public Authentication authenticate(String email, String password) {
        return authentication_manager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    /**
     * @param authentication
     * @return User
     */
    @Override
    public User get_user_info(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    /**
     * @param request
     * @param response
     * @param authentication
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            final String refreshToken = jwtService.getRefreshTokenFromCookie(request);
            String email = jwtService.getEmailFromRefreshToken(refreshToken);
            jwtService.removeByEmail(email);
        } catch (Exception e) {
            log.error("Error while logging out: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            Cookie accessCookie = new Cookie(ConstantUtils.JWT_ACCESS_TOKEN, null);
            accessCookie.setPath("/");
            accessCookie.setHttpOnly(true);
            accessCookie.setMaxAge(0);
            response.addCookie(accessCookie);

            // Clear refresh token cookie
            Cookie refreshTokenCookie = new Cookie(ConstantUtils.JWT_REFRESH_TOKEN, null);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            // Clear security context
            SecurityContextHolder.clearContext();
        }
    }
}
