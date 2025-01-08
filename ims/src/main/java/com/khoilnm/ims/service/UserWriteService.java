package com.khoilnm.ims.service;

import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.model.User;

public interface UserWriteService {
    User createUser(RegistrationDTO registrationDTO);
}
