package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.ApplicationCreateRequest;
import com.agriloan.dto.ApplicationDTO;
import com.agriloan.dto.AuditRequest;
import com.agriloan.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 贷款申请接口：增删改查 + 全流程状态流转
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /** 列表：农户/企业仅本人，审批员为全部 */
    @GetMapping
    public ApiResponse<List<ApplicationDTO>> list(@RequestParam(required = false) String status) {
        return ApiResponse.ok(applicationService.list(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationDTO> detail(@PathVariable String id) {
        return ApiResponse.ok(applicationService.detail(id));
    }

    /** 提交贷款申请（农户 / 农业企业） */
    @PostMapping
    public ApiResponse<ApplicationDTO> create(@Valid @RequestBody ApplicationCreateRequest request) {
        return ApiResponse.ok(applicationService.create(request));
    }

    /** 撤回申请：仅申请人本人、且处于待初审状态（增删改查中的「删」） */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable String id) {
        return ApiResponse.ok(applicationService.delete(id));
    }

    /** 签约：初审通过 → 已签约 */
    @PostMapping("/{id}/sign")
    public ApiResponse<ApplicationDTO> sign(@PathVariable String id) {
        return ApiResponse.ok(applicationService.sign(id));
    }

    /** 放款：已签约 → 已放款 */
    @PostMapping("/{id}/loan")
    public ApiResponse<ApplicationDTO> loan(@PathVariable String id) {
        return ApiResponse.ok(applicationService.loan(id));
    }

    /** 结清：已放款 → 已结清 */
    @PostMapping("/{id}/settle")
    public ApiResponse<ApplicationDTO> settle(@PathVariable String id) {
        return ApiResponse.ok(applicationService.settle(id));
    }

    /** 初审：通过 / 拒绝（仅审批人员，见 SecurityConfig） */
    @PostMapping("/{id}/audit")
    public ApiResponse<ApplicationDTO> audit(@PathVariable String id,
                                             @Valid @RequestBody AuditRequest request) {
        return ApiResponse.ok(applicationService.audit(id, request));
    }
}
