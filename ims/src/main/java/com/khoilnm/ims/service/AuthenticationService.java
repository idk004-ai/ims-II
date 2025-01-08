package com.khoilnm.ims.service;

import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.model.User;
import org.springframework.security.core.Authentication;


public interface AuthenticationService {

    Authentication authenticate(String email, String password);

    User get_user_info(Authentication authentication);

}
