package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.UserRepository;
import com.khoilnm.ims.service.UserWriteService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserWriteServiceImpl implements UserWriteService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserWriteServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * @param registrationDTO
     * @return
     */
    @Override
    public User createUser(RegistrationDTO registrationDTO) {
        User user = User.builder()
                .fullName(registrationDTO.getFirstname() + " " + registrationDTO.getLastname())
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .gender(ConstantUtils.MALE)
                .departmentId(ConstantUtils.IT)
                .roleId(ConstantUtils.ADMIN_ROLE)
                .enabled(true)
                .build();
        userRepository.save(user);
        return user;
    }
}
