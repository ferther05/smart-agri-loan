package com.agriloan.repository;

import com.agriloan.domain.ApplyStatus;
import com.agriloan.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    List<LoanApplication> findByApplicantIdOrderByApplyTimeDesc(Long applicantId);

    List<LoanApplication> findByApplicantIdAndStatusOrderByApplyTimeDesc(Long applicantId, ApplyStatus status);

    List<LoanApplication> findByStatusOrderByApplyTimeDesc(ApplyStatus status);

    List<LoanApplication> findAllByOrderByApplyTimeDesc();

    long countByStatus(ApplyStatus status);

    long countByApplicantId(Long applicantId);

    /** 已放款金额合计（用于风控看板 / 首页统计） */
    @Query("select coalesce(sum(a.amount), 0) from LoanApplication a where a.status in :statuses")
    BigDecimal sumAmountByStatusIn(@Param("statuses") List<ApplyStatus> statuses);

    @Query("select coalesce(sum(a.amount), 0) from LoanApplication a where a.applicant.id = :userId and a.status in :statuses")
    BigDecimal sumAmountByApplicantAndStatusIn(@Param("userId") Long userId,
                                               @Param("statuses") List<ApplyStatus> statuses);
}
