package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 贷款申请状态机
 *
 * <pre>
 * 待初审 --初审通过--> 初审通过 --签约--> 已签约 --放款--> 已放款 --结清--> 已结清
 *    \--拒绝--> 已拒绝（终态）
 * </pre>
 *
 * <p>序列化为中文标签，与前端 constants.js 中的 APPLY_STATUS 完全一致</p>
 */
public enum ApplyStatus {

    PENDING_FIRST("待初审"),
    FIRST_PASS("初审通过"),
    SIGNED("已签约"),
    LOANED("已放款"),
    SETTLED("已结清"),
    REJECTED("已拒绝");

    private final String label;

    ApplyStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ApplyStatus of(String value) {
        if (value == null) {
            return null;
        }
        for (ApplyStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) || status.label.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知申请状态：" + value);
    }
}
