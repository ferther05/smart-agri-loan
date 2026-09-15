package com.agriloan.dto;

import lombok.Data;

import java.util.List;

/**
 * 贷款产品（返回给前端展示）
 *
 * <p>canApply / applyTip 由后端按当前登录人的信用等级计算，
 * 前端据此实现"不同用户看到不同界面"：低信用用户按钮置灰并给出原因。</p>
 */
@Data
public class ProductDTO {

    private String id;

    private String name;

    private String type;

    private String badge;

    private String iconText;

    private Double rate;

    private Integer termMonths;

    private List<Integer> terms;

    private Long minAmount;

    private Long maxAmount;

    private String repayment;

    private String guarantee;

    private String target;

    private List<String> features;

    private Integer applyCount;

    private Boolean hot;

    /** 准入门槛：最低信用等级 */
    private String minCreditLevel;

    /** 是否在售（false = 已下架；管理端列表据此区分） */
    private Boolean enabled;

    /** 当前用户是否可以申请 */
    private Boolean canApply = true;

    /** 不可申请的原因 / 申请提示 */
    private String applyTip = "";
}
