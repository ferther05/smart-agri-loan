package com.agriloan.service;

import com.agriloan.dto.PolicyDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 信用等级策略中心 —— "自适应界面"的唯一判定来源
 *
 * <p>等级划分（与前端 constants.js 一致）：</p>
 * <ul>
 *   <li>AAA 850-950：信用极好，最高 50 万纯信用，利率下浮 30BP，极速审批</li>
 *   <li>AA&nbsp; 750-849：信用良好，最高 30 万纯信用，利率下浮 15BP，极速审批</li>
 *   <li>A&nbsp;&nbsp; 650-749：信用一般，25 万以下需补充经营材料，常规审批</li>
 *   <li>B&nbsp;&nbsp; 550-649：信用待提升，需担保增信，额度受限</li>
 *   <li>C&nbsp;&nbsp; 0-549&nbsp;&nbsp;：暂不满足纯信用授信，建议补充担保 / 修复征信</li>
 * </ul>
 */
@Service
public class CreditPolicyService {

    public static final int SCORE_MIN = 350;

    public static final int SCORE_MAX = 950;

    /** 等级由高到低，用于比较准入门槛 */
    private static final List<String> LEVELS = List.of("AAA", "AA", "A", "B", "C");

    public String levelOf(Integer score) {
        int value = score == null ? 0 : score;
        if (value >= 850) {
            return "AAA";
        }
        if (value >= 750) {
            return "AA";
        }
        if (value >= 650) {
            return "A";
        }
        if (value >= 550) {
            return "B";
        }
        return "C";
    }

    /** 等级权重：AAA=5 ... C=1，越大越优 */
    public int rank(String level) {
        int index = LEVELS.indexOf(level);
        return index < 0 ? 0 : LEVELS.size() - index;
    }

    /** 判断用户等级是否达到产品门槛 */
    public boolean meets(String userLevel, String requiredLevel) {
        return rank(userLevel) >= rank(requiredLevel);
    }

    public String commentOf(Integer score) {
        int value = score == null ? 0 : score;
        if (value >= 850) {
            return "信用极好";
        }
        if (value >= 750) {
            return "信用良好";
        }
        if (value >= 650) {
            return "信用一般";
        }
        if (value >= 550) {
            return "信用待提升";
        }
        return "信用较弱";
    }

    /** 下一等级与还差多少分 */
    public String nextLevelOf(String level) {
        int index = LEVELS.indexOf(level);
        if (index <= 0) {
            return null;
        }
        return LEVELS.get(index - 1);
    }

    public int scoreToNextLevel(Integer score) {
        String level = levelOf(score);
        String next = nextLevelOf(level);
        if (next == null) {
            return 0;
        }
        int target = switch (next) {
            case "AAA" -> 850;
            case "AA" -> 750;
            case "A" -> 650;
            case "B" -> 550;
            default -> 0;
        };
        return Math.max(0, target - (score == null ? 0 : score));
    }

    /** 当前等级权益清单 */
    public List<String> benefitsOf(String level) {
        return switch (level) {
            case "AAA" -> List.of(
                    "最高 50 万纯信用额度，免抵押直接授信",
                    "首贷利率下浮 30BP，融资成本更低",
                    "极速审批通道，最快 1 天完成放款",
                    "无还本续贷资格，到期资金无缝衔接");
            case "AA" -> List.of(
                    "最高 30 万纯信用额度，免抵押申请",
                    "首贷利率下浮 15BP",
                    "1.5 天极速审批通道",
                    "无还本续贷资格");
            case "A" -> List.of(
                    "纯信用额度最高 15 万",
                    "补充土地确权、经营流水材料后可提额",
                    "常规审批通道（约 3 个工作日）",
                    "可申请抵押类、政策类贷款");
            case "B" -> List.of(
                    "纯信用额度最高 5 万",
                    "需提供保证人或农业保险增信",
                    "建议优先选择抵押类贷款",
                    "按时还款可逐步提升等级");
            default -> List.of(
                    "暂不满足纯信用授信条件",
                    "建议补充担保、抵押物或政策性保险增信",
                    "可先申请小额政策性贷款建立信用档案",
                    "修复征信后重新评估");
        };
    }

    /** 自适应提示语：首页 / 信用页横幅 */
    public String noticeOf(String level) {
        return switch (level) {
            case "AAA" -> "信用极好：可直接申请纯信用贷款，享受最高额度与最优利率";
            case "AA" -> "信用良好：可直接申请纯信用贷款，最高 30 万并享利率优惠";
            case "A" -> "信用一般：可申请纯信用贷款，建议补充经营材料以提升额度";
            case "B" -> "信用待提升：申请需提供担保或保险增信，建议优先选择抵押类产品";
            default -> "信用较弱：暂不支持纯信用贷款，建议补充担保增信并修复征信";
        };
    }

    public String suggestionOf(String level) {
        return switch (level) {
            case "AAA", "AA" -> "保持按时还款，可进一步享受续贷利率优惠";
            case "A" -> "完善土地确权信息、购买农业保险，预计可提升 10-15 分";
            case "B" -> "结清现有小额贷款并保持 6 个月无逾期，可提升至 A 级";
            default -> "建议先还清逾期款项，并申请政策性农业保险建立信用记录";
        };
    }

    /** 完整政策对象 */
    public PolicyDTO policyOf(String level) {
        PolicyDTO dto = new PolicyDTO();
        dto.setLevel(level);
        dto.setNotice(noticeOf(level));
        dto.setSuggestion(suggestionOf(level));
        switch (level) {
            case "AAA" -> {
                dto.setLevelName("信用极好");
                dto.setMaxPureCreditAmount(new BigDecimal("500000"));
                dto.setRateDiscountBp(30);
                dto.setFastTrack(true);
                dto.setRequiresGuarantee(false);
                dto.setCanApplyCreditLoan(true);
                dto.setApprovalDesc("极速通道，最快 1 天放款");
            }
            case "AA" -> {
                dto.setLevelName("信用良好");
                dto.setMaxPureCreditAmount(new BigDecimal("300000"));
                dto.setRateDiscountBp(15);
                dto.setFastTrack(true);
                dto.setRequiresGuarantee(false);
                dto.setCanApplyCreditLoan(true);
                dto.setApprovalDesc("极速通道，最快 1.5 天放款");
            }
            case "A" -> {
                dto.setLevelName("信用一般");
                dto.setMaxPureCreditAmount(new BigDecimal("150000"));
                dto.setRateDiscountBp(0);
                dto.setFastTrack(false);
                dto.setRequiresGuarantee(false);
                dto.setCanApplyCreditLoan(true);
                dto.setApprovalDesc("常规审批，约 3 个工作日");
            }
            case "B" -> {
                dto.setLevelName("信用待提升");
                dto.setMaxPureCreditAmount(new BigDecimal("50000"));
                dto.setRateDiscountBp(0);
                dto.setFastTrack(false);
                dto.setRequiresGuarantee(true);
                dto.setCanApplyCreditLoan(true);
                dto.setApprovalDesc("需担保增信，约 5 个工作日");
            }
            default -> {
                dto.setLevelName("信用较弱");
                dto.setMaxPureCreditAmount(BigDecimal.ZERO);
                dto.setRateDiscountBp(0);
                dto.setFastTrack(false);
                dto.setRequiresGuarantee(true);
                dto.setCanApplyCreditLoan(false);
                dto.setApprovalDesc("暂不支持纯信用授信");
            }
        }
        return dto;
    }
}
