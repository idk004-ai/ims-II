package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.exceptions.TokenCreationException;
import com.khoilnm.ims.model.RefreshToken;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.RefreshTokenRepository;
import com.khoilnm.ims.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.jwt.access_token.expiration_time}")
    private Long accessTokenExpirationTime;

    @Value("${app.jwt.refresh_token.expiration_time}")
    private Long refreshTokenExpirationTime;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @param user
     * @return
     */
    @Override
    public String createAccessToken(User user) {

        try {
            log.info("Creating access token for user: {}", user.getEmail());

            Date date = new Date();
            Date expirationDate = new Date(date.getTime() + accessTokenExpirationTime * 1000);

            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
            claims.put("email", user.getEmail());

            String token = Jwts
                    .builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(expirationDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            log.info("Access token created for user: {}", user.getEmail());
            return token;
        } catch (Exception e) {
            log.error("Error when creating access token: {}", e.getMessage());
            throw new TokenCreationException("Error when creating access token", e);
        }
    }

    /**
     * @param user
     * @return
     */
    @Override
    public String createRefreshToken(User user) {
        try {
            log.info("Creating refresh token for user: {}", user.getName());

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpirationTime * 1000);

            // Generate token series
            String tokenSeries = generateTokenSeries();

            Map<String, Object> claims = new HashMap<>();
            claims.put("type", "refresh");
            claims.put("series", tokenSeries);
            claims.put("email", user.getEmail());

            String token = Jwts
                    .builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

            saveRefreshToken(user, token, tokenSeries, expiryDate);

            log.info("Refresh token created for user: {}", user.getEmail());

            return token;
        } catch (Exception e) {
            log.error("Error when creating refresh token: {}", e.getMessage());
            throw new TokenCreationException("Error when creating refresh token", e);
        }
    }

    /**
     * @param response
     * @param accessToken
     */
    @Override
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ConstantUtils.JWT_ACCESS_TOKEN, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(accessTokenExpirationTime));
        response.addCookie(cookie);
    }

    /**
     * @param response
     * @param refreshToken
     */
    @Override
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(ConstantUtils.JWT_REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(refreshTokenExpirationTime));
        response.addCookie(cookie);
    }

    /**
     * @param response
     */
    @Override
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(ConstantUtils.JWT_REFRESH_TOKEN, null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * @param response
     */
    @Override
    public void removeAccessTokenCookie(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(ConstantUtils.JWT_ACCESS_TOKEN, null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);
    }

    /**
     * @param request
     * @return
     */
    @Override
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(ConstantUtils.JWT_REFRESH_TOKEN)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * @param refreshToken
     * @return
     */
    @Override
    public String getEmailFromRefreshToken(String refreshToken) {
        return extractClaims(refreshToken, (Claims claims) -> claims.get("email", String.class));
    }

    /**
     * @param refreshToken
     * @return
     */
    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, ConstantUtils.JWT_REFRESH_TOKEN);
    }

    @Override
    @Transactional
    public void removeByEmail(String email) {
        try {
            if (!refreshTokenRepository.existsByUserEmail(email)) {
                log.info("No refresh token found for email: {}", email);
                return;
            }

            int deletedCount = refreshTokenRepository.deleteByEmail(email);
            if (deletedCount > 0) {
                log.info("Successfully deleted {} refresh token(s) for email: {}", deletedCount, email);
            } else {
                log.info("No refresh tokens were deleted for email: {}", email);
            }
        } catch (Exception e) {
            log.error("Error while deleting refresh token for email {}: {}", email, e.getMessage());
            throw new TokenCreationException("Error when deleting refresh token", e);
        }
    }

    private boolean validateToken(String token, String tokenType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Validate token type for refresh tokens
            if (ConstantUtils.JWT_REFRESH_TOKEN.equals(tokenType)) {
                String type = claims.get("type", String.class);
                if (!ConstantUtils.JWT_REFRESH_TOKEN.equals(type)) {
                    log.warn("Invalid token type: expected refresh token");
                    return false;
                }

                // Validate against stored refresh token
                String email = claims.getSubject();
                String series = claims.get("series", String.class);
                return validateStoredRefreshToken(email, token, series);
            }

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Token is expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Token validation error", e);
        }
        return false;
    }

    @Transactional(readOnly = true)
    protected boolean validateStoredRefreshToken(String email, String token, String series) {
        return refreshTokenRepository.findByEmailAndTokenAndSeries(email, token, series).isPresent();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Transactional(readOnly = true)
    protected void saveRefreshToken(User user, String refreshToken, String tokenSeries, Date expiryDate) {
        try {
            log.info("Saving refresh token for user: {}", user.getEmail());

            removeByEmail(user.getEmail());

            log.info("Length of refresh token: {}", refreshToken.length());
            log.info("Refresh token: {}", refreshToken);

            RefreshToken refreshTokenObject = RefreshToken.builder()
                    .user(user)
                    .token(refreshToken)
                    .series(tokenSeries)
                    .expiryDate(expiryDate)
                    .deleteFlag(false)
                    .build();

            refreshTokenRepository.save(refreshTokenObject);
            log.info("Refresh token saved to database for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error when saving refresh token: {}", e.getMessage());
            throw new TokenCreationException("Error when saving refresh token", e);
        }
    }

    private String generateTokenSeries() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

}
