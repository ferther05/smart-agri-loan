package com.agriloan.repository;

import com.agriloan.domain.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {
}
