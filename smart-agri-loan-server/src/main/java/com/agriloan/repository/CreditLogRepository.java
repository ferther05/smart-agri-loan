package com.agriloan.repository;

import com.agriloan.domain.CreditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditLogRepository extends JpaRepository<CreditLog, String> {

    List<CreditLog> findByUserIdOrderByDateDescIdDesc(Long userId);

    List<CreditLog> findTop10ByUserIdOrderByDateDescIdDesc(Long userId);
}
