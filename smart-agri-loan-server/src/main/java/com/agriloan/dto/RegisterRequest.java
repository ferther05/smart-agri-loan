package com.agriloan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求：农户 / 农业企业自助注册，审批人员由后台开通
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "请输入登录账号")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "账号需为 4-20 位字母、数字或下划线")
    private String username;

    @NotBlank(message = "请输入登录密码")
    @Size(min = 6, max = 30, message = "密码长度需在 6-30 位之间")
    private String password;

    @NotBlank(message = "请输入姓名或主体名称")
    private String name;

    /** 角色：农户 / 农业企业（注册页选择，不允许注册审批人员） */
    private String role;

    @Pattern(regexp = "^1[3-9]\\d{9}$|^$", message = "手机号格式不正确")
    private String phone;

    private String address;

    private String business;

    private String scale;
}
