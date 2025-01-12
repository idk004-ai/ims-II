package com.khoilnm.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationDTO implements UserDTO{
    private int id;
    private String email;
    private String userName;
    private String fullName;
    private String phone;
    private int roleId;
    private int statusId;
    private int departmentId;
    private int genderId;
    private String address;
    private LocalDate dob;
    private String note;
}
