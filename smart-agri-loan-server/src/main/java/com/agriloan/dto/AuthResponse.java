package com.agriloan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录 / 注册成功返回：JWT + 当前用户信息 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String tokenType = "Bearer";

    /** 有效期（秒） */
    private long expiresIn;

    private UserDTO user;

    public AuthResponse(String token, long expiresIn, UserDTO user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
