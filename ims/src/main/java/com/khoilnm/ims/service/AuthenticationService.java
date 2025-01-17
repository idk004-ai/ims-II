package com.khoilnm.ims.service;

import com.khoilnm.ims.exceptions.TokenRevocationException;
import com.khoilnm.ims.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;


public interface AuthenticationService extends LogoutHandler {

    Authentication authenticate(String email, String password);

    String createPasswordResetUrl(@NotNull User user);

    boolean validateResetToken(String token);

    void markTokenAsUsed(String email);

    void changePassword(String email, String newPassword) throws TokenRevocationException;
}
