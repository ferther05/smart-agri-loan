package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 账户接口：余额充值（演示用，便于构造还款场景）
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final ApplicationService applicationService;

    @PostMapping("/recharge")
    public ApiResponse<Map<String, Object>> recharge(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal balance = applicationService.recharge(body.get("amount"));
        return ApiResponse.ok(Map.of("balance", balance));
    }
}
