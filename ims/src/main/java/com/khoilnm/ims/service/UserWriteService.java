package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;

public interface UserWriteService<T> {
    User createUser(T object);
}
