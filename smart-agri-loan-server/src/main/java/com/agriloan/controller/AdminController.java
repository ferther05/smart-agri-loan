package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.NewsDTO;
import com.agriloan.dto.ProductDTO;
import com.agriloan.service.NewsService;
import com.agriloan.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端全量查询（仅审批人员，见 SecurityConfig）
 *
 * <p>与公开接口的区别：这里包含已下架数据，供后台维护列表使用；
 * 新增 / 修改仍走各自的资源接口（POST /api/products、PUT /api/news/{id} 等）。</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;

    private final NewsService newsService;

    /** 全部贷款产品（含已下架） */
    @GetMapping("/products")
    public ApiResponse<List<ProductDTO>> products() {
        return ApiResponse.ok(productService.listAll());
    }

    /** 全部惠农资讯（含已下架） */
    @GetMapping("/news")
    public ApiResponse<List<NewsDTO>> news() {
        return ApiResponse.ok(newsService.listAll());
    }
}
