package com.agriloan.controller;

import com.agriloan.common.ApiResponse;
import com.agriloan.dto.NewsDTO;
import com.agriloan.dto.NewsSaveRequest;
import com.agriloan.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 惠农资讯接口
 *
 * <ul>
 *   <li>GET 列表 / 详情：公开只读</li>
 *   <li>POST / PUT：审批人员维护资讯（新增、修改、上下架），权限见 SecurityConfig</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ApiResponse<List<NewsDTO>> list(@RequestParam(required = false) String category) {
        return ApiResponse.ok(newsService.list(category));
    }

    @GetMapping("/latest")
    public ApiResponse<List<NewsDTO>> latest() {
        return ApiResponse.ok(newsService.latest());
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsDTO> detail(@PathVariable String id) {
        return ApiResponse.ok(newsService.detail(id));
    }

    /** 新增资讯（审批人员）：编号留空自动生成 */
    @PostMapping
    public ApiResponse<NewsDTO> create(@Valid @RequestBody NewsSaveRequest request) {
        return ApiResponse.ok(newsService.create(request));
    }

    /** 修改资讯（审批人员）：未传的字段保持原值，enabled=false 即下架 */
    @PutMapping("/{id}")
    public ApiResponse<NewsDTO> update(@PathVariable String id,
                                      @Valid @RequestBody NewsSaveRequest request) {
        return ApiResponse.ok(newsService.update(id, request));
    }
}
