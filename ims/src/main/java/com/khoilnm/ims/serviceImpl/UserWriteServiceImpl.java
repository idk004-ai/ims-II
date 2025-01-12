package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.common.PasswordUtils;
import com.khoilnm.ims.dto.RegistrationDTO;
import com.khoilnm.ims.dto.UserCreationDTO;
import com.khoilnm.ims.dto.UserDTO;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.UserRepository;
import com.khoilnm.ims.service.UserWriteService;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserWriteServiceImpl implements UserWriteService<UserDTO> {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public UserWriteServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, MessageSource messageSource) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }


    private User createUser(RegistrationDTO registrationDTO) {
        User user = User.builder()
                .email(registrationDTO.getEmail())
                ._username(ConstantUtils.FIRST_USERNAME)
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .fullName(registrationDTO.getFirstname() + " " + registrationDTO.getLastname())
                .departmentId(ConstantUtils.IT)
                .roleId(ConstantUtils.ADMIN_ROLE)
                .status(ConstantUtils.ACTIVE)
                .gender(ConstantUtils.MALE)
                .phone(registrationDTO.getPhone())
                .dob(registrationDTO.getDob())
                .note("")
                .enabled(true)
                .deleteFlag(false)
                .build();
        user.setCreatedBy(1);
        return user;
    }

    private User createUser(UserCreationDTO userDTO) {
        String password = PasswordUtils.generateRandomPassword(8);
        return User.builder()
                .email(userDTO.getEmail())
                ._username(userDTO.getUserName())
                .password(passwordEncoder.encode(password))
                .fullName(userDTO.getFullName())
                .departmentId(userDTO.getDepartmentId())
                .roleId(userDTO.getRoleId())
                .status(userDTO.getStatusId())
                .gender(userDTO.getGenderId())
                .phone(userDTO.getPhone())
                .dob(userDTO.getDob())
                .note(userDTO.getNote())
                .enabled(false)
                .deleteFlag(false)
                .build();
    }

    /**
     * @param object
     * @return
     */
    @Override
    public User createUser(UserDTO object) {
        User user = new User();
        if (object instanceof RegistrationDTO) {
            user = createUser((RegistrationDTO) object);
        } else if (object instanceof UserCreationDTO) {
            user = createUser((UserCreationDTO) object);
        } else {
            String error = messageSource.getMessage("ME008", null, Locale.getDefault());
            throw new IllegalArgumentException(error);
        }
        userRepository.save(user);
        return user;
    }
}
