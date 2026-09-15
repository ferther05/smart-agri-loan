package com.agriloan.service;

import com.agriloan.common.PageResult;
import com.agriloan.domain.OperationLog;
import com.agriloan.repository.OperationLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AOP 操作日志查询服务（审批人员 / 运维使用）
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Transactional(readOnly = true)
    public PageResult<OperationLog> query(String keyword, String module, Boolean success,
                                          int page, int size) {
        Specification<OperationLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("action"), like),
                        cb.like(root.get("target"), like),
                        cb.like(root.get("ip"), like)));
            }
            if (module != null && !module.isBlank()) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "operateTime"));
        Page<OperationLog> result = operationLogRepository.findAll(spec, pageable);
        return PageResult.of(result);
    }

    /** 日志模块下拉选项 */
    @Transactional(readOnly = true)
    public List<String> modules() {
        return List.of("认证", "贷款申请", "审批", "账户", "信用");
    }
}
