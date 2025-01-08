package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authentication_manager;

    public AuthenticationServiceImpl(AuthenticationManager authentication_manager) {
        this.authentication_manager = authentication_manager;
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

}
