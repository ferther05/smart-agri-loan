package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AOP 操作日志：由 {@code OperationLogAspect} 自动写入，
 * 记录"谁在什么时间、什么 IP、对哪个模块做了什么操作、是否成功"。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_operation_log", indexes = {
        @Index(name = "idx_oplog_username", columnList = "username"),
        @Index(name = "idx_oplog_time", columnList = "operate_time")
})
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作账号 */
    @Column(length = 50)
    private String username;

    /** 操作角色（中文） */
    @Column(length = 20)
    private String role;

    /** 业务模块：认证 / 贷款申请 / 审批 / 信用 / 账户 */
    @Column(length = 50)
    private String module;

    /** 操作描述，如"提交贷款申请" */
    @Column(length = 100)
    private String action;

    /** 目标对象，如申请编号 SQ20260901001 */
    @Column(length = 100)
    private String target;

    /** 请求方法签名 */
    @Column(length = 200)
    private String method;

    /** 入参摘要（截断保存） */
    @Column(length = 1000)
    private String params;

    @Column(length = 50)
    private String ip;

    /** 是否成功 */
    @Column(nullable = false)
    private Boolean success = true;

    @Column(length = 500)
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "operate_time")
    private LocalDateTime operateTime;
}
