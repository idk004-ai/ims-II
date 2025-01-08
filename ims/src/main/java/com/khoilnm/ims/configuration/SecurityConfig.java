package com.khoilnm.ims.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtFilterConfig jwtFilterConfig;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private static final String[] WHITE_LIST_URL = {
            "/auth/**",
            "/common/**",
            "/static/**",
            "/assets/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };

    public SecurityConfig(JwtFilterConfig jwtFilterConfig, AuthenticationProvider authenticationProvider, LogoutHandler logoutHandler) {
        this.jwtFilterConfig = jwtFilterConfig;
        this.authenticationProvider = authenticationProvider;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(login -> login
                        .loginPage("/auth/login")
                        .permitAll()
                        .defaultSuccessUrl("/api/v1/home", true))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilterConfig, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(
                                (request, response, authentication) -> SecurityContextHolder.clearContext())
                        .permitAll())
                .build();

    }

}
