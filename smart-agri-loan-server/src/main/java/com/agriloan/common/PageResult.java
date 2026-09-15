package com.agriloan.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;

    private long total;

    private int page;

    private int size;

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(),
                page.getNumber() + 1, page.getSize());
    }
}
