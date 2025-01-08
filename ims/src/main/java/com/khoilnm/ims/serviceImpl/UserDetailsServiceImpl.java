package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;


    public UserDetailsServiceImpl(UserRepository userRepository, MessageSource message_source) {
        this.userRepository = userRepository;
        this.messageSource = message_source;
    }

    /**
     * <p>This function finds {@link com.khoilnm.ims.model.User User}</p>
     * <p>If function can not find out {@link com.khoilnm.ims.model.User User} , function throws Exception with message "User not found"</p>
     *
     * @param email
     * @return {@link com.khoilnm.ims.model.User User}
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    String userMessage = messageSource.getMessage("ME000", null, Locale.getDefault());
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException(userMessage);
                });
    }
}
