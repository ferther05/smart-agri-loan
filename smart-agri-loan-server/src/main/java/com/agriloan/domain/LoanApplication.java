package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 贷款申请单（核心业务实体）
 *
 * <p>状态流转：待初审 → 初审通过 → 已签约 → 已放款 → 已结清，或中途被拒绝</p>
 */
@Getter
@Setter
@Entity
@Table(name = "loan_application", indexes = {
        @Index(name = "idx_apply_applicant", columnList = "applicant_id"),
        @Index(name = "idx_apply_status", columnList = "status")
})
public class LoanApplication extends Auditable {

    /** 申请编号，如 SQ20260901001 */
    @Id
    @Column(length = 30)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 产品名称快照：列表展示无需联表 */
    @Column(nullable = false, length = 100)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @Column(nullable = false, length = 100)
    private String applicantName;

    @Column(length = 30)
    private String phone;

    /** 脱敏身份证号 */
    @Column(length = 40)
    private String idCard;

    /** 申请金额（元） */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** 期限（月） */
    @Column(nullable = false)
    private Integer term;

    @Column(length = 300)
    private String purpose;

    /** 还款来源说明 */
    @Column(length = 300)
    private String repaySource;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String business;

    @Column(length = 50)
    private String scale;

    /** 申请时的信用积分快照（风控留痕） */
    @Column(nullable = false)
    private Integer creditScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplyStatus status = ApplyStatus.PENDING_FIRST;

    /** 利率快照（%） */
    @Column(nullable = false)
    private Double rate = 0d;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime auditTime;

    @Column(length = 300)
    private String auditOpinion;

    @Column(length = 50)
    private String auditor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime signedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime loanTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime settleTime;

    /** 结清金额（本息合计） */
    @Column(precision = 18, scale = 2)
    private BigDecimal repayAmount = BigDecimal.ZERO;

    /** 流转日志（提交、初审、签约、放款、结清） */
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ApplicationLog> logs = new ArrayList<>();

    public void addLog(String action, String operator, String note) {
        addLogAt(LocalDateTime.now(), action, operator, note);
    }

    public void addLogAt(LocalDateTime time, String action, String operator, String note) {
        ApplicationLog log = new ApplicationLog();
        log.setApplication(this);
        log.setTime(time);
        log.setAction(action);
        log.setOperator(operator);
        log.setNote(note);
        this.logs.add(log);
    }
}
