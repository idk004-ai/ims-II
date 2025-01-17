package com.khoilnm.ims.controller;

import com.khoilnm.ims.common.Base32Utils;
import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.common.EmailTemplate;
import com.khoilnm.ims.dto.LoginDTO;
import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.dto.UserDTO;
import com.khoilnm.ims.exceptions.TokenRefreshException;
import com.khoilnm.ims.exceptions.TokenRevocationException;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.*;
import jakarta.mail.MessagingException;
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

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserReadService userReadService;
    private final UserWriteService<UserDTO, User> userWriteService;
    private final EmailService emailService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UserReadService userReadService,
            UserWriteService<UserDTO, User> userWriteService,
            EmailService emailService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userReadService = userReadService;
        this.userWriteService = userWriteService;
        this.emailService = emailService;
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> createResetToken(@RequestParam String email) {
        try {
            return userReadService.findByEmail(email)
                    .map(user -> {
                        try {
                            String resetUrl = authenticationService.createPasswordResetUrl(user);

                            // Prepare email props
                            Map<String, Object> props = new HashMap<>();
                            props.put("resetLink", resetUrl);
                            props.put("userName", user.get_username());

                            emailService.sendEmail(user.getEmail(),
                                    "Password reset",
                                    EmailTemplate.RESET_PASSWORD,
                                    props,
                                    false);

                            return ResponseEntity.ok()
                                    .body("Password reset link sent to email");

                        } catch (MessagingException e) {
                            log.error("Failed to send password reset email to {}: {}",
                                    email, e.getMessage(), e);
                            return ResponseEntity
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Failed to send password reset email. Please try again later.");
                        }
                    })
                    .orElseGet(() -> {
                        log.warn("Password reset attempted for non-existent email: {}", email);
                        return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body("If the email exists in our system, you will receive a password reset link.");
                    });

        } catch (Exception e) {
            log.error("Unexpected error in password reset process: {}",
                    e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody String newPassword) {
        try {
            if (!authenticationService.validateResetToken(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
            }
            User user = userReadService.findByResetToken(token);

            // TODO: Implement password reset logic
            authenticationService.changePassword(user.getEmail(), newPassword);

            // TODO: Mark token as used
            authenticationService.markTokenAsUsed(user.getEmail());


            return ResponseEntity.ok().body("Reset password");
        } catch (Exception | TokenRevocationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
