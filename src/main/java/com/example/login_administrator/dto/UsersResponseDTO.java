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

//    @Override
//    public String toString() {
//        return "UsersResponseDTO{" +
//                "userId=" + userId +
//                ", username='" + username + '\'' +
//                ", password='" + password + '\'' +
//                '}';
//    }
}
