package com.khoilnm.ims.service;

import com.khoilnm.ims.model.User;

import java.util.Date;

public interface UserWriteService<T, U> {
    User createUser(T object);
    void updateResetPasswordToken(String token, Date expiryDate, String email);

    void update(U user);
}
