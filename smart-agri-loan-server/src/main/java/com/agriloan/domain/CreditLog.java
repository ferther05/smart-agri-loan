package com.agriloan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 信用积分变动日志
 */
@Getter
@Setter
@Entity
@Table(name = "credit_log", indexes = @Index(name = "idx_credit_user", columnList = "user_id"))
public class CreditLog extends Auditable {

    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 变动日期 yyyy-MM-dd */
    @Column(length = 20)
    private String date;

    /** 事件说明 */
    @Column(length = 200)
    private String event;

    /** 变动分值（可为负） */
    @Column(nullable = false)
    private Integer delta = 0;

    /** 变动后积分 */
    @Column(nullable = false)
    private Integer score = 0;

    /** 维度类型：repay / business / asset / record / policy */
    @Column(length = 20)
    private String type;
}
