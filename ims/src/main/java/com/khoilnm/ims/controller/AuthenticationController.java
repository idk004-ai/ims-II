package com.khoilnm.ims.controller;

import com.khoilnm.ims.dto.LoginDTO;
import com.khoilnm.ims.dto.RegistrationDTO;
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
    private final UserWriteService userWriteService;

    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService, UserDetailsService userDetailsService, UserReadService userReadService, UserWriteService userWriteService) {
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
            userReadService.findByEmail(request.getEmail());
            userWriteService.createUser(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationService.authenticate(loginDTO.getUsername(), loginDTO.getPassword());
            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user);

            jwtService.setAccessTokenCookie(response, accessToken);
            jwtService.setRefreshTokenCookie(response, refreshToken);


            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.debug("Refreshing access token");

            String refreshToken = jwtService.getRefreshTokenFromCookie(request);
            if (!jwtService.validateRefreshToken(refreshToken)) {
                log.warn("Invalid refresh token");
                throw new TokenRefreshException("Invalid refresh token");
            }

            String email = jwtService.getEmailFromRefreshToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            User user = (User) userDetails;
            String accessToken = jwtService.createAccessToken(user);
            jwtService.setAccessTokenCookie(response, accessToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> postMethodName(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) {
        authenticationService.logout(request, response, authentication);
        return ResponseEntity.ok().build();
    }
}
