package com.agriloan.support;

import com.agriloan.domain.LoanApplication;
import com.agriloan.domain.News;
import com.agriloan.domain.Product;
import com.agriloan.domain.User;
import com.agriloan.dto.*;
import com.agriloan.service.CreditPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实体 -> DTO 转换器（同时负责"自适应"字段计算）
 */
@Component
@RequiredArgsConstructor
public class DtoMapper {

    private final CreditPolicyService policyService;

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRole().getLabel());
        dto.setRoleCode(user.getRole().name());
        dto.setPhone(user.getPhone());
        dto.setIdCard(user.getIdCard());
        dto.setAddress(user.getAddress());
        dto.setBusiness(user.getBusiness());
        dto.setScale(user.getScale());
        dto.setCreditScore(user.getCreditScore());
        dto.setBalance(user.getBalance());

        String level = policyService.levelOf(user.getCreditScore());
        dto.setCreditLevel(level);
        dto.setCreditComment(policyService.commentOf(user.getCreditScore()));
        dto.setBenefits(policyService.benefitsOf(level));
        dto.setDimensions(List.of(
                new DimensionDTO("repay", "履约能力", user.getRepayScore()),
                new DimensionDTO("business", "经营稳定", user.getBusinessScore()),
                new DimensionDTO("asset", "资产状况", user.getAssetScore()),
                new DimensionDTO("record", "信用记录", user.getRecordScore()),
                new DimensionDTO("policy", "政策匹配", user.getPolicyScore())));
        return dto;
    }

    public ProductDTO toProductDTO(Product product, User currentUser) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setType(product.getType().getLabel());
        dto.setBadge(product.getBadge());
        dto.setIconText(product.getIconText());
        dto.setRate(product.getRate());
        dto.setTermMonths(product.getTermMonths());
        dto.setTerms(splitInts(product.getTerms()));
        dto.setMinAmount(product.getMinAmount());
        dto.setMaxAmount(product.getMaxAmount());
        dto.setRepayment(product.getRepayment());
        dto.setGuarantee(product.getGuarantee());
        dto.setTarget(product.getTarget());
        dto.setFeatures(splitStrings(product.getFeatures()));
        dto.setApplyCount(product.getApplyCount());
        dto.setHot(product.getHot());
        dto.setMinCreditLevel(product.getMinCreditLevel());
        dto.setEnabled(product.getEnabled());

        // ---------- 自适应准入判断 ----------
        if (currentUser == null) {
            dto.setCanApply(true);
            dto.setApplyTip("");
            return dto;
        }
        if ("BANK_ADMIN".equals(currentUser.getRole().name())) {
            dto.setCanApply(false);
            dto.setApplyTip("审批人员账号不参与贷款申请");
            return dto;
        }
        String userLevel = policyService.levelOf(currentUser.getCreditScore());
        if (!policyService.meets(userLevel, product.getMinCreditLevel())) {
            dto.setCanApply(false);
            dto.setApplyTip("需信用等级 " + product.getMinCreditLevel() + " 及以上，当前为 " + userLevel + " 级");
            return dto;
        }
        PolicyDTO policy = policyService.policyOf(userLevel);
        if (Boolean.TRUE.equals(policy.getRequiresGuarantee())) {
            dto.setApplyTip("信用等级 " + userLevel + "：申请需补充担保或保险增信材料");
        } else if (Boolean.TRUE.equals(policy.getFastTrack())) {
            dto.setApplyTip("信用等级 " + userLevel + "：可走极速审批通道（" + policy.getApprovalDesc() + "）");
        } else {
            dto.setApplyTip("信用等级 " + userLevel + "：" + policy.getSuggestion());
        }
        return dto;
    }

    public NewsDTO toNewsDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setCategory(news.getCategory().getLabel());
        dto.setSource(news.getSource());
        dto.setDate(news.getDate());
        dto.setViews(news.getViews());
        dto.setSummary(news.getSummary());
        dto.setContent(splitParagraphs(news.getContent()));
        dto.setEnabled(news.getEnabled());
        return dto;
    }

    public ApplicationDTO toApplicationDTO(LoanApplication application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setProductId(application.getProduct() == null ? null : application.getProduct().getId());
        dto.setProductName(application.getProductName());
        dto.setApplicantId(application.getApplicant() == null ? null : application.getApplicant().getId());
        dto.setApplicantName(application.getApplicantName());
        dto.setPhone(application.getPhone());
        dto.setIdCard(application.getIdCard());
        dto.setAmount(application.getAmount());
        dto.setTerm(application.getTerm());
        dto.setPurpose(application.getPurpose());
        dto.setRepaySource(application.getRepaySource());
        dto.setAddress(application.getAddress());
        dto.setBusiness(application.getBusiness());
        dto.setScale(application.getScale());
        dto.setCreditScore(application.getCreditScore());
        dto.setStatus(application.getStatus().getLabel());
        dto.setRate(application.getRate());
        dto.setApplyTime(application.getApplyTime());
        dto.setUpdateTime(application.getUpdateTime());
        dto.setAuditTime(application.getAuditTime());
        dto.setAuditOpinion(application.getAuditOpinion());
        dto.setAuditor(application.getAuditor());
        dto.setSignedTime(application.getSignedTime());
        dto.setLoanTime(application.getLoanTime());
        dto.setSettleTime(application.getSettleTime());
        dto.setRepayAmount(application.getRepayAmount());
        dto.setRepayTotal(repayTotal(application));
        // 复制为普通集合：避免懒加载代理在事务外序列化失败
        dto.setLogs(new ArrayList<>(application.getLogs()));

        RiskView risk = riskOf(application.getCreditScore());
        dto.setRiskLevel(risk.label());
        dto.setRiskType(risk.type());
        return dto;
    }

    /** 到期应还本息合计 = 本金 × (1 + 年利率 × 期限月数 / 12) */
    public BigDecimal repayTotal(LoanApplication application) {
        BigDecimal amount = application.getAmount() == null ? BigDecimal.ZERO : application.getAmount();
        double rate = application.getRate() == null ? 0 : application.getRate();
        int term = application.getTerm() == null ? 0 : application.getTerm();
        BigDecimal factor = BigDecimal.valueOf(1 + (rate / 100) * term / 12);
        return amount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public record RiskView(String label, String type) {
    }

    /** 按信用积分给出红黄绿风险等级（与前端 U.riskOf 一致） */
    public RiskView riskOf(Integer score) {
        int value = score == null ? 0 : score;
        if (value >= 750) {
            return new RiskView("低风险", "success");
        }
        if (value >= 650) {
            return new RiskView("中风险", "warn");
        }
        return new RiskView("高风险", "danger");
    }

    private List<Integer> splitInts(String text) {
        List<Integer> list = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return list;
        }
        for (String item : text.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(Integer.valueOf(trimmed));
            }
        }
        return list;
    }

    private List<String> splitStrings(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split("[;；]")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private List<String> splitParagraphs(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split("\\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
