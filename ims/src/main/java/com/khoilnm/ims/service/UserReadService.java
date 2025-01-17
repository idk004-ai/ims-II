package com.khoilnm.ims.service;

import com.khoilnm.ims.dto.UserDisplayDTO;
import com.khoilnm.ims.model.User;

import java.util.List;
import java.util.Optional;

public interface UserReadService {
    Optional<User> findByEmail(String email);

    List<UserDisplayDTO> getAllUsers(int page, int pageSize);

    User findByResetToken(String token);
}
