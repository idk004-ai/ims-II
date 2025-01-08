package com.khoilnm.ims.configuration;

import com.khoilnm.ims.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilterConfig extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final List<String> publicPaths = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    );

    public JwtFilterConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::contains);
    }

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (isPublicPath(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String refreshToken = jwtService.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email = jwtService.getEmailFromRefreshToken(refreshToken);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtService.validateRefreshToken(refreshToken)) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
