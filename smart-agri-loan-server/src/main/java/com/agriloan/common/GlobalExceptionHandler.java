package com.agriloan.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理：保证任何异常都以统一 JSON 结构返回，前端可统一 toast 提示
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 参数校验失败：取第一条错误信息返回 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ApiResponse.fail(400, msg.isEmpty() ? "参数校验不通过" : msg);
    }

    /** 请求体格式错误（如未使用 JSON、JSON 语法错误） */
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public ApiResponse<Void> handleBadBody(Exception e) {
        return ApiResponse.fail(400, "请求参数格式有误，请使用 JSON 提交");
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethod(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.fail(405, "请求方法不支持：" + e.getMethod());
    }

    /** 无权限（角色不足）：前端据此隐藏或提示 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleDenied(AccessDeniedException e) {
        return ApiResponse.fail(403, "当前角色无权执行该操作");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthenticationException e) {
        return ApiResponse.fail(401, "登录状态已失效，请重新登录");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        log.error("服务异常", e);
        return ApiResponse.fail(500, "服务器开小差了，请稍后重试");
    }
}
