package com.khoilnm.ims.controller;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.dto.LoginDTO;
import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.dto.UserDTO;
import com.khoilnm.ims.exceptions.TokenRefreshException;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserReadService userReadService;
    private final UserWriteService<UserDTO> userWriteService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService, UserDetailsService userDetailsService, UserReadService userReadService, UserWriteService<UserDTO> userWriteService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userReadService = userReadService;
        this.userWriteService = userWriteService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationDTO request) {
        try {
            log.info("Registering user with email: {}", request.getEmail());
            if (userReadService.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
            }
            User user = userWriteService.createUser(request);
            return ResponseEntity.accepted().body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationService.authenticate(loginDTO.getEmail(), loginDTO.getPassword());
            User user = (User) authentication.getPrincipal();

            String accessToken = jwtService.createAccessToken(user);
            jwtService.setAccessTokenCookie(response, accessToken);

            log.info("Is remember me; " + loginDTO.isRememberMe());

            if (loginDTO.isRememberMe()) {
                String refreshToken = jwtService.createRefreshToken(user, true);
                jwtService.setRefreshTokenCookie(response, refreshToken);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = ConstantUtils.JWT_REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            log.warn("Refresh token is missing");
            clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            if (!jwtService.validateRefreshToken(refreshToken, request)) {
                log.warn("Invalid refresh token");
                clearAuthCookies(response);
                throw new TokenRefreshException("Invalid refresh token");
            }

            String email = jwtService.getEmailFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            User user = (User) userDetails;
            String accessToken = jwtService.createAccessToken(user);
            jwtService.setAccessTokenCookie(response, accessToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        jwtService.removeAccessTokenCookie(response);
        jwtService.removeRefreshTokenCookie(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) {
        try {
            authenticationService.logout(request, response, authentication);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
