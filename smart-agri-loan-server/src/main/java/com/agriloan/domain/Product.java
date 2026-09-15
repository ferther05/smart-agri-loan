package com.agriloan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 贷款产品
 */
@Getter
@Setter
@Entity
@Table(name = "loan_product")
public class Product extends Auditable {

    /** 产品编号，如 P001（由后台指定，非自增） */
    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    /** 角标文案，如"纯信用""政策贴息" */
    @Column(length = 20)
    private String badge;

    /** 卡片图标文字，如"春" */
    @Column(length = 4)
    private String iconText;

    /** 年化利率（%） */
    @Column(nullable = false)
    private Double rate;

    /** 主推期限（月） */
    @Column(nullable = false)
    private Integer termMonths;

    /** 可选期限，逗号分隔，如 "6,12,18" */
    @Column(length = 50)
    private String terms;

    /** 起贷金额（元） */
    @Column(nullable = false)
    private Long minAmount;

    /** 最高额度（元） */
    @Column(nullable = false)
    private Long maxAmount;

    @Column(length = 50)
    private String repayment;

    @Column(length = 100)
    private String guarantee;

    @Column(length = 200)
    private String target;

    /** 产品亮点，分号分隔 */
    @Column(length = 300)
    private String features;

    /** 累计申请次数 */
    @Column(nullable = false)
    private Integer applyCount = 0;

    /** 是否热门 */
    @Column(nullable = false)
    private Boolean hot = false;

    /**
     * 准入门槛：允许申请的最低信用等级（AAA/AA/A/B/C）
     * 前端据此对低信用用户禁用"立即申请"，实现差异化界面
     */
    @Column(nullable = false, length = 4)
    private String minCreditLevel = "C";

    @Column(nullable = false)
    private Boolean enabled = true;
}
