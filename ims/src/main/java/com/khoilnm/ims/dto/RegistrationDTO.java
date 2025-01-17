package com.khoilnm.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegistrationDTO implements UserDTO {

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    @NotEmpty(message = "{ME005}")
    @NotBlank(message = "{ME005}")
    private String username;
}
