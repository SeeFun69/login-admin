package com.example.login_administrator.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
}
