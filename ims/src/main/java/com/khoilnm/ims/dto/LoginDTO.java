package com.khoilnm.ims.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotNull(message = "ME001")
    @NotBlank(message = "ME001")
    @Email(message = "ME001")
    private String email;

    @NotNull(message = "ME002")
    @NotNull(message = "ME002")
    private String password;

    @JsonProperty("isRememberMe")
    private boolean rememberMe;
}
