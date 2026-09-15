package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.CreditInfoDTO;
import com.agriloan.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信用积分接口：当前登录人的积分、等级、五维画像与等级权益
 */
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/me")
    public ApiResponse<CreditInfoDTO> me() {
        return ApiResponse.ok(creditService.infoOfCurrentUser());
    }
}
