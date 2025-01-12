package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.exceptions.TokenCreationException;
import com.khoilnm.ims.exceptions.TokenValidationException;
import com.khoilnm.ims.model.RefreshToken;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.RefreshTokenRepository;
import com.khoilnm.ims.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Transactional
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    private static final long ACCESS_TOKEN_VALIDITY = 900L; // 15 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 604800L; // 7 days

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @param user User
     * @return String
     */
    @Override
    public String createAccessToken(User user) {
        log.info("Creating access token for user: {}", user.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        claims.put("email", user.getEmail());

        String token = Jwts
                .builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setIssuer(applicationName)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("Access token created for user: {}", user.getEmail());
        return token;
    }

    /**
     * @param user         User
     * @param isRememberMe boolean
     * @return String
     */
    @Override
    public String createRefreshToken(User user, boolean isRememberMe) {
        log.info("Creating refresh token for user: {}", user.getName());

        Date expiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("remember_me", isRememberMe);

        String token = Jwts
                .builder()
                .setClaims(claims)
                .setSubject(ConstantUtils.JWT_REFRESH_TOKEN)
                .setIssuedAt(new Date())
                .setIssuer(applicationName)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        saveRefreshToken(user, token, expiryDate, isRememberMe);

        log.info("Refresh token created for user: {}", user.getEmail());

        return token;
    }

    /**
     * @param response    HttpServletResponse
     * @param accessToken String
     */
    @Override
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ConstantUtils.JWT_ACCESS_TOKEN, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(ACCESS_TOKEN_VALIDITY));
        response.addCookie(cookie);
    }

    /**
     * @param response     HttpServletResponse
     * @param refreshToken String
     */
    @Override
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(ConstantUtils.JWT_REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(REFRESH_TOKEN_VALIDITY));
        response.addCookie(cookie);
    }

    /**
     * @param response HttpServletResponse
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
     * @param response HttpServletResponse
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
     * @param request HttpServletRequest
     * @return String
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
     * @param token String
     * @return String
     */
    @Override
    public String getEmailFromToken(String token) {
        return extractClaims(token, (Claims claims) -> claims.get("email", String.class));
    }

    /**
     * @param request HttpServletRequest
     * @return String
     */
    @Override
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(ConstantUtils.JWT_ACCESS_TOKEN)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * @param refreshToken String
     * @param request      HttpServletRequest
     * @return boolean
     */
    @Override
    public boolean validateRefreshToken(String refreshToken, HttpServletRequest request) {
        try {
            Claims claims = extractAllClaims(refreshToken);

            RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new TokenValidationException("Refresh token not found in database"));

            if (storedRefreshToken.isRevoke()) {
                log.warn("Attempt to use revoked token for email: {}", storedRefreshToken.getEmail());
                throw new TokenValidationException("Refresh token has been revoked");
            }

            if (storedRefreshToken.getExpiryDate().before(new Date())) {
                log.warn("Expired refresh token used for email: {}", storedRefreshToken.getEmail());
                throw new TokenValidationException("Refresh token has expired");
            }

            validateTokenClaims(claims, storedRefreshToken);

            log.info("Refresh token validated successfully for email: {}", storedRefreshToken.getEmail());
            return true;
        } catch (TokenValidationException e) {
            log.error("Error while validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    private void validateTokenClaims(Claims claims, RefreshToken storedRefreshToken) {
        // validate email
        String emailFromClaims = claims.get("email", String.class);
        if (emailFromClaims == null || !emailFromClaims.equals(storedRefreshToken.getEmail())) {
            log.warn("Email mismatch in refresh token");
            throw new TokenValidationException("Email mismatch in refresh token");
        }

        // validate remember me status
        Boolean isRememberMe = claims.get("remember_me", Boolean.class);
        if (isRememberMe == null || isRememberMe != storedRefreshToken.isRememberMe()) {
            log.warn("Remember me status mismatch in refresh token");
            throw new TokenValidationException("Remember me status mismatch in refresh token");
        }
    }

    /**
     * @param accessToken String
     * @return boolean
     */
    @Override
    public boolean validateAccessToken(String accessToken) {
        if (accessToken.isEmpty()) {
            log.warn("Access token is empty");
            return false;
        }
        try {
            Claims claims = extractAllClaims(accessToken);

            if (claims.getExpiration().before(new Date())) {
                log.warn("Access token is expired");
                return false;
            }
            return true;
        } catch (TokenValidationException e) {
            log.error("Error while validating access token: {}", e.getMessage());
            return false;
        }
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            throw new TokenValidationException("Invalid JWT token", e);
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            throw new TokenValidationException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            throw new TokenValidationException("JWT claims string is empty", e);
        } catch (Exception e) {
            throw new TokenValidationException("Error while parsing JWT token", e);
        }
    }

    @Override
    public void removeByEmail(String email) {
        try {
            if (refreshTokenRepository.findAllByEmail(email).isEmpty()) {
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

    protected void saveRefreshToken(User user, String refreshToken, Date expiryDate, boolean isRememberMe) {
        log.info("Saving refresh token for user: {}", user.getEmail());
        RefreshToken refreshTokenObject = RefreshToken.builder()
                .email(user.getEmail())
                .token(refreshToken)
                .isRevoke(false)
                .isRememberMe(isRememberMe)
                .expiryDate(expiryDate)
                .deleteFlag(false)
                .build();

        refreshTokenObject.setCreatedBy(user.getId());

        refreshTokenRepository.save(refreshTokenObject);
        log.info("Refresh token saved to database for user: {}", user.getEmail());
    }

    /**
     * Revoke refresh token
     *
     * @param token  String
     * @param reason String
     */
    @Override
    public void revokeToken(String token, String reason) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("Refresh token not found in database"));

        refreshToken.setRevoke(true);
        refreshToken.setRevokedAt(new Date());
        refreshToken.setRevokeReason(reason);

        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked successfully for email: {}", refreshToken.getEmail());
    }

    /**
     * Revoke all refresh tokens for a user
     *
     * @param email  String
     * @param reason String
     */
    @Override
    public void revokeAllTokens(String email, String reason) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByEmail(email);

        tokens.forEach(token -> {
            token.setRevoke(true);
            token.setRevokedAt(new Date());
            token.setRevokeReason(reason);
        });

        refreshTokenRepository.saveAll(tokens);

        log.info("All refresh tokens revoked successfully for email: {}", email);
    }
}
