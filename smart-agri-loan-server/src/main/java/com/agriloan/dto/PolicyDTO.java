package com.agriloan.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 信用等级对应的授信政策（后端统一下发，前端按此做自适应界面）
 */
@Data
public class PolicyDTO {

    private String level;

    /** 等级说明，如"信用良好" */
    private String levelName;

    /** 最高纯信用额度（元），0 表示暂不支持纯信用授信 */
    private BigDecimal maxPureCreditAmount;

    /** 利率下浮（BP） */
    private Integer rateDiscountBp;

    /** 是否享受极速审批通道 */
    private Boolean fastTrack;

    /** 是否必须补充担保 / 增信材料 */
    private Boolean requiresGuarantee;

    /** 是否可以申请纯信用类贷款 */
    private Boolean canApplyCreditLoan;

    /** 审批时效说明 */
    private String approvalDesc;

    /** 首页 / 信用页顶部提示语（自适应文案） */
    private String notice;

    /** 提额 / 提分建议 */
    private String suggestion;
}
