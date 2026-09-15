package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.ProductDTO;
import com.agriloan.dto.ProductSaveRequest;
import com.agriloan.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 贷款产品接口
 *
 * <ul>
 *   <li>GET 列表 / 详情：公开（游客可浏览，登录后附带"是否可申请"的自适应判定）</li>
 *   <li>POST / PUT：审批人员维护产品（新增、修改、上下架），权限见 SecurityConfig</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductDTO>> list(@RequestParam(required = false) String type,
                                              @RequestParam(required = false) String sort) {
        return ApiResponse.ok(productService.list(type, sort));
    }

    @GetMapping("/hot")
    public ApiResponse<List<ProductDTO>> hot() {
        return ApiResponse.ok(productService.hotList());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(productService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDTO> detail(@PathVariable String id) {
        return ApiResponse.ok(productService.detail(id));
    }

    /** 新增产品（审批人员）：编号留空自动生成 */
    @PostMapping
    public ApiResponse<ProductDTO> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    /** 修改产品（审批人员）：未传的字段保持原值，enabled=false 即下架 */
    @PutMapping("/{id}")
    public ApiResponse<ProductDTO> update(@PathVariable String id,
                                         @Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }
}
