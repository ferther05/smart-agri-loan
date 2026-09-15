package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.UserDTO;
import com.agriloan.dto.UserSaveRequest;
import com.agriloan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口（仅审批人员，见 SecurityConfig）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserDTO>> list() {
        return ApiResponse.ok(userService.listAll());
    }

    /** 新增账号（审批人员 / 农户 / 农业企业均可由管理员开通） */
    @PostMapping
    public ApiResponse<UserDTO> create(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    /** 修改用户资料 / 信用档案 / 启用停用；password 留空表示不改密码 */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> update(@PathVariable Long id,
                                       @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    /** 人工调整信用积分（风控线下核实后录入） */
    @PostMapping("/{id}/credit")
    public ApiResponse<UserDTO> adjustCredit(@PathVariable Long id, @RequestBody CreditAdjustRequest request) {
        return ApiResponse.ok(userService.adjustCredit(id, request.delta(), request.event(), request.type()));
    }

    public record CreditAdjustRequest(int delta, String event, String type) {
    }
}
