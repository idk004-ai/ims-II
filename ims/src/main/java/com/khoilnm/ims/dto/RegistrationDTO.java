package com.khoilnm.ims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationDTO {

    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    private String firstname;
    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    private String lastname;
    @Email(message = "{ME005}")
    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    private String email;
    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    @Size(min = 8, message = "{ME006}")
    private String password;
    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    private String phone;
}
