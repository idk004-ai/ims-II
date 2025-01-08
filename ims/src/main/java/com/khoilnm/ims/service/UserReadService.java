package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;

public interface UserReadService {
    User findByEmail(String email);
}
