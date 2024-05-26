package com.example.login_administrator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginDto {
    @NotBlank(message = "Email is required!")
    @Email(message = "The email address is invalid.")
    private String email;

    @NotBlank(message = "Password is required!")
    @Size(min = 8, message = "The length of password must be at least 8 characters.")
    private String password;
}
