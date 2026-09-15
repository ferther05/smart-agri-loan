package com.agriloan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "请输入登录账号")
    private String username;

    @NotBlank(message = "请输入登录密码")
    private String password;
}
