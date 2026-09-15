package com.agriloan.common;

import lombok.Data;

/**
 * 统一响应体：前端 api.js 依赖 code / message / data 三个字段
 *
 * @param <T> 业务数据类型
 */
@Data
public class ApiResponse<T> {

    /** 200 成功；401 未登录/登录失效；403 无权限；400 参数或业务错误；500 服务异常 */
    private int code;

    private String message;

    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = 200;
        res.message = "success";
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = code;
        res.message = message;
        return res;
    }
}
