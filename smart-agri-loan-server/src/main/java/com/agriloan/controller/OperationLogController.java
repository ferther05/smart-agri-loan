package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.common.PageResult;
import com.agriloan.domain.OperationLog;
import com.agriloan.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AOP 操作日志管理接口（仅审批人员）
 */
@RestController
@RequestMapping("/api/oplog")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public ApiResponse<PageResult<OperationLog>> query(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String module,
                                                       @RequestParam(required = false) Boolean success,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(operationLogService.query(keyword, module, success, page, size));
    }

    @GetMapping("/modules")
    public ApiResponse<List<String>> modules() {
        return ApiResponse.ok(operationLogService.modules());
    }
}
