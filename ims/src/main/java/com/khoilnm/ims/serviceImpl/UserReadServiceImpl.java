package com.khoilnm.ims.serviceImpl;

import com.khoilnm.ims.dto.UserDisplayDTO;
import com.khoilnm.ims.mapper.UserMapper;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.repository.UserRepository;
import com.khoilnm.ims.service.UserReadService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserReadServiceImpl implements UserReadService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserMapper usermapper;

    public UserReadServiceImpl(UserRepository userRepository, MessageSource messageSource, UserMapper usermapper) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.usermapper = usermapper;
    }

    /**
     * @param email
     * @return
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * <p>Find all registered user</p>
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public List<UserDisplayDTO> getAllUsers(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> users = userRepository.findAll(pageable);
        List<User> listOfUsers = users.getContent();
        return listOfUsers.stream().map(usermapper::toUserDTO).toList();
    }

    /**
     * @param token String
     * @return User
     */
    @Override
    public User findByResetToken(String token) {
        return userRepository.findByResetPasswordToken(token).orElseThrow(() -> new NoSuchElementException("Reset token not found"));
    }
}
