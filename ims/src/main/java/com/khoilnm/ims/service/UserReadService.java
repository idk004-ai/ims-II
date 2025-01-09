package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;

import java.util.Optional;

public interface UserReadService {
    Optional<User> findByEmail(String email);
}
