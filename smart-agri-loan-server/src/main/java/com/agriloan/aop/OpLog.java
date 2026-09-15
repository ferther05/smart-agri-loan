package com.agriloan.aop;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注在需要留痕的业务方法上，由 {@link OperationLogAspect} 统一记录。
 *
 * <pre>
 * &#64;OpLog(module = "贷款申请", action = "提交贷款申请", target = "#request.productId")
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /** 业务模块，如：认证 / 贷款申请 / 审批 / 信用 / 账户 */
    String module();

    /** 操作描述 */
    String action();

    /** 目标对象表达式（SpEL，可引用方法参数，如 #id、#request.applicantName），可空 */
    String target() default "";
}
