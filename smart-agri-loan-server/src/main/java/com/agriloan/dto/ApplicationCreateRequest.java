package com.agriloan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/** 提交贷款申请 */
@Data
public class ApplicationCreateRequest {

    @NotBlank(message = "请选择贷款产品")
    private String productId;

    @NotNull(message = "请输入申请金额")
    @DecimalMin(value = "1", message = "申请金额必须大于 0")
    private BigDecimal amount;

    @NotNull(message = "请选择贷款期限")
    private Integer term;

    @NotBlank(message = "请填写贷款用途")
    @Size(min = 5, max = 300, message = "贷款用途不少于 5 个字")
    private String purpose;

    @NotBlank(message = "请输入申请人姓名")
    private String name;

    @Pattern(regexp = "^[1-9]\\d{5}(?:19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$",
            message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String address;

    private String business;

    private String scale;

    @NotBlank(message = "请填写还款来源")
    @Size(min = 5, max = 300, message = "还款来源不少于 5 个字")
    private String repaySource;

    @AssertTrue(message = "请先勾选征信查询授权")
    private Boolean agreeCredit;

    @AssertTrue(message = "请先确认信息真实性承诺")
    private Boolean agreeTruth;
}
