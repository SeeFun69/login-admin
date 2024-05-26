package com.example.login_administrator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    @NotBlank(message = "name is required!")
    private String name;

    @NotBlank(message = "phone is required!")
    @Pattern(regexp = "^[0-9]+$", message="phone value must be number")
    @Size(min = 10, max = 18, message = "The length of phone must be between 10 and 18 characters.")
    private String phone;

    @NotBlank(message = "Email is required!")
    @Email(message = "The email address is invalid.")
    private String email;
}
