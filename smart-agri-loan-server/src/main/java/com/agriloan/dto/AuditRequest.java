package com.agriloan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 审批操作（初审通过 / 拒绝） */
@Data
public class AuditRequest {

    /** pass=初审通过，reject=拒绝 */
    @NotBlank(message = "请选择审批动作")
    @Pattern(regexp = "^(pass|reject)$", message = "审批动作不合法")
    private String action;

    @NotBlank(message = "请填写审批意见")
    private String opinion;
}
