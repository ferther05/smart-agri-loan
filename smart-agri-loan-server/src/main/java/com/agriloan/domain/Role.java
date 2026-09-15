package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 系统角色：决定前端菜单显隐与接口访问权限
 *
 * <p>说明：序列化为中文标签，前端导航按此判断</p>
 */
public enum Role {

    /** 农户：可查看自己的信用与申请，发起贷款、签约、还款 */
    FARMER("农户"),

    /** 农业企业：权限同农户，额度与准入规则不同 */
    ENTERPRISE("农业企业"),

    /** 审批人员（金融机构）：初审通过/拒绝、查看风控看板与操作日志，不发起贷款 */
    BANK_ADMIN("审批人员");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /** Spring Security 权限名 */
    public String authority() {
        return "ROLE_" + name();
    }

    public static Role of(String value) {
        if (value == null) {
            return FARMER;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value) || role.label.equals(value)) {
                return role;
            }
        }
        return FARMER;
    }
}
