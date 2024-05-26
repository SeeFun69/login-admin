package com.example.login_administrator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithPageDto {
    List<UsersResponseDTO> users;

    int page;

    @JsonProperty("page_available")
    int pageAvailable;
}
