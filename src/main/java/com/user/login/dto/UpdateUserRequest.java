package com.user.login.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long userId;

    private String name;
    private String username;
    private String password;


}
