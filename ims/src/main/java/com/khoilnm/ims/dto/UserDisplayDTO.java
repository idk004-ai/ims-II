package com.khoilnm.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserDisplayDTO extends BaseDTO implements UserDTO{
    private Integer id;
    private String fullname;
    private String username;
    private String phoneNo;
    private String role;
    private String status;
    private String email;
    private String gender;
    private String address;
    private LocalDate dob;
    private String department;
    private String note;
}
