package com.agriloan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 申请单流转日志 */
@Getter
@Setter
@Entity
@Table(name = "loan_application_log")
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private LoanApplication application;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime time;

    @Column(length = 50)
    private String action;

    @Column(length = 50)
    private String operator;

    @Column(length = 300)
    private String note;
}
