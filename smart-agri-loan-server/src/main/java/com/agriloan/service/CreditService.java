package com.agriloan.service;

import com.agriloan.common.BusinessException;
import com.agriloan.domain.User;
import com.agriloan.dto.CreditInfoDTO;
import com.agriloan.dto.DimensionDTO;
import com.agriloan.repository.CreditLogRepository;
import com.agriloan.repository.UserRepository;
import com.agriloan.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 信用积分服务：积分、等级、五维画像、变动日志与等级权益（自适应下发）
 */
@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;

    private final CreditLogRepository creditLogRepository;

    private final CreditPolicyService policyService;

    @Transactional(readOnly = true)
    public CreditInfoDTO infoOfCurrentUser() {
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "账号不存在，请重新登录"));

        String level = policyService.levelOf(user.getCreditScore());
        CreditInfoDTO dto = new CreditInfoDTO();
        dto.setScore(user.getCreditScore());
        dto.setLevel(level);
        dto.setComment(policyService.commentOf(user.getCreditScore()));
        dto.setScoreMin(CreditPolicyService.SCORE_MIN);
        dto.setScoreMax(CreditPolicyService.SCORE_MAX);
        dto.setNextLevel(policyService.nextLevelOf(level));
        dto.setToNextLevel(policyService.scoreToNextLevel(user.getCreditScore()));
        dto.setDimensions(List.of(
                new DimensionDTO("repay", "履约能力", user.getRepayScore()),
                new DimensionDTO("business", "经营稳定", user.getBusinessScore()),
                new DimensionDTO("asset", "资产状况", user.getAssetScore()),
                new DimensionDTO("record", "信用记录", user.getRecordScore()),
                new DimensionDTO("policy", "政策匹配", user.getPolicyScore())));
        dto.setLogs(creditLogRepository.findByUserIdOrderByDateDescIdDesc(userId));
        dto.setBenefits(policyService.benefitsOf(level));
        dto.setPolicy(policyService.policyOf(level));
        return dto;
    }
}
