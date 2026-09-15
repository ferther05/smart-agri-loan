package com.agriloan.dto;

import com.agriloan.domain.ApplicationLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 贷款申请单（与前端 db.js 中的申请单结构保持一致，前端无需改动模板）
 */
@Data
public class ApplicationDTO {

    private String id;

    private String productId;

    private String productName;

    private Long applicantId;

    private String applicantName;

    private String phone;

    private String idCard;

    private BigDecimal amount;

    private Integer term;

    private String purpose;

    private String repaySource;

    private String address;

    private String business;

    private String scale;

    private Integer creditScore;

    /** 中文状态，与前端状态常量一致 */
    private String status;

    private Double rate;

    private LocalDateTime applyTime;

    private LocalDateTime updateTime;

    private LocalDateTime auditTime;

    private String auditOpinion;

    private String auditor;

    private LocalDateTime signedTime;

    private LocalDateTime loanTime;

    private LocalDateTime settleTime;

    private BigDecimal repayAmount;

    /** 应还本息合计（后端算好，前端直接展示） */
    private BigDecimal repayTotal;

    /** 风险等级：低风险 / 中风险 / 高风险 */
    private String riskLevel;

    private String riskType;

    private List<ApplicationLog> logs;
}
