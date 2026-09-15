package com.agriloan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 系统用户（农户 / 农业企业 / 审批人员）
 *
 * <p>信用积分与五维评分是"自适应界面"的判定依据：
 * 不同信用等级的用户看到的产品准入、权益与提示都不一样</p>
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user", indexes = @Index(name = "uk_user_username", columnList = "username", unique = true))
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号 */
    @Column(nullable = false, length = 50, unique = true)
    private String username;

    /** BCrypt 加密后的密码 */
    @Column(nullable = false, length = 100)
    private String password;

    /** 姓名 / 主体名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 角色 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.FARMER;

    @Column(length = 30)
    private String phone;

    @Column(length = 40)
    private String idCard;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String business;

    @Column(length = 50)
    private String scale;

    /** 信用积分（350 ~ 950） */
    @Column(nullable = false)
    private Integer creditScore = 700;

    /** 账户余额（元） */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /* ---------- 五维信用画像（0 ~ 100），供雷达图自适应展示 ---------- */

    /** 履约能力 */
    @Column(nullable = false)
    private Integer repayScore = 60;

    /** 经营稳定 */
    @Column(nullable = false)
    private Integer businessScore = 60;

    /** 资产状况 */
    @Column(nullable = false)
    private Integer assetScore = 60;

    /** 信用记录 */
    @Column(nullable = false)
    private Integer recordScore = 60;

    /** 政策匹配 */
    @Column(nullable = false)
    private Integer policyScore = 60;

    @Column(nullable = false)
    private Boolean enabled = true;
}
