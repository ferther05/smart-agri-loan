package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.AuthResponse;
import com.agriloan.dto.LoginRequest;
import com.agriloan.dto.RegisterRequest;
import com.agriloan.dto.UserDTO;
import com.agriloan.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口：注册 / 登录 / 当前用户
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册（农户 / 农业企业） */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /** 登录：返回 JWT 与用户信息（含角色，前端据此渲染菜单） */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /** 当前登录人信息 */
    @GetMapping("/me")
    public ApiResponse<UserDTO> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
