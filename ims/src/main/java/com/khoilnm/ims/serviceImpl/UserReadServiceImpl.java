package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.UserRepository;
import com.khoilnm.ims.service.UserReadService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserReadServiceImpl implements UserReadService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public UserReadServiceImpl(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    /**
     * @param email
     * @return
     */
    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            String error = messageSource.getMessage("ME004", new Object[]{email}, Locale.getDefault());
            return new RuntimeException(error);
        });
        return user;
    }
}
