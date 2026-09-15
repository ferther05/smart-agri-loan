package com.agriloan.service;

import com.agriloan.aop.OpLog;
import com.agriloan.common.BusinessException;
import com.agriloan.domain.*;
import com.agriloan.dto.ApplicationCreateRequest;
import com.agriloan.dto.ApplicationDTO;
import com.agriloan.dto.AuditRequest;
import com.agriloan.dto.PolicyDTO;
import com.agriloan.repository.CreditLogRepository;
import com.agriloan.repository.LoanApplicationRepository;
import com.agriloan.repository.ProductRepository;
import com.agriloan.repository.UserRepository;
import com.agriloan.security.LoginUser;
import com.agriloan.security.SecurityUtils;
import com.agriloan.support.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 贷款申请服务：完整业务闭环
 *
 * <pre>
 * 提交申请(农户/企业) → 初审通过/拒绝(审批员) → 签约(申请人) → 放款(申请人/系统) → 结清(申请人)
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final BigDecimal MAX_RECHARGE = new BigDecimal("1000000");

    private final LoanApplicationRepository applicationRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final CreditLogRepository creditLogRepository;

    private final CreditPolicyService policyService;

    private final DtoMapper dtoMapper;

    /* ==================== 查询 ==================== */

    /** 列表：审批员看全部，农户/企业只看自己的单子 */
    @Transactional(readOnly = true)
    public List<ApplicationDTO> list(String status) {
        List<LoanApplication> list;
        if (SecurityUtils.isBankAdmin()) {
            if (status == null || status.isBlank() || "全部".equals(status)) {
                list = applicationRepository.findAllByOrderByApplyTimeDesc();
            } else {
                list = applicationRepository.findByStatusOrderByApplyTimeDesc(ApplyStatus.of(status));
            }
        } else {
            Long userId = SecurityUtils.currentUserId();
            if (status == null || status.isBlank() || "全部".equals(status)) {
                list = applicationRepository.findByApplicantIdOrderByApplyTimeDesc(userId);
            } else {
                list = applicationRepository.findByApplicantIdAndStatusOrderByApplyTimeDesc(userId, ApplyStatus.of(status));
            }
        }
        return list.stream().map(dtoMapper::toApplicationDTO).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDTO detail(String id) {
        return dtoMapper.toApplicationDTO(loadAndCheckPermission(id));
    }

    /* ==================== 用户侧操作 ==================== */

    /** 提交贷款申请 */
    @OpLog(module = "贷款申请", action = "提交贷款申请", target = "#request.productId")
    @Transactional
    public ApplicationDTO create(ApplicationCreateRequest request) {
        User user = requireApplicant();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("贷款产品不存在或已下架"));
        if (!Boolean.TRUE.equals(product.getEnabled())) {
            throw new BusinessException("该产品已停售");
        }

        // 金额范围校验：使用 BigDecimal 精确比较，避免 longValue 截断小数导致超限金额绕过
        BigDecimal amount = request.getAmount();
        BigDecimal minAmount = BigDecimal.valueOf(product.getMinAmount());
        BigDecimal maxAmount = BigDecimal.valueOf(product.getMaxAmount());
        if (amount.compareTo(minAmount) < 0 || amount.compareTo(maxAmount) > 0) {
            throw new BusinessException(String.format("申请金额需在 %,d ~ %,d 元之间",
                    product.getMinAmount(), product.getMaxAmount()));
        }
        // 期限校验
        List<Integer> terms = List.of(product.getTerms().split(",")).stream().map(String::trim)
                .filter(s -> !s.isEmpty()).map(Integer::valueOf).toList();
        if (!terms.contains(request.getTerm())) {
            throw new BusinessException("所选期限不在该产品可选范围内");
        }

        // ---------- 自适应准入：按信用等级放行 ----------
        String level = policyService.levelOf(user.getCreditScore());
        if (!policyService.meets(level, product.getMinCreditLevel())) {
            throw new BusinessException(String.format(
                    "当前信用等级为 %s 级，该产品要求 %s 级及以上，暂时无法申请",
                    level, product.getMinCreditLevel()));
        }
        PolicyDTO policy = policyService.policyOf(level);
        if (product.getType() == ProductType.CREDIT
                && !Boolean.TRUE.equals(policy.getCanApplyCreditLoan())) {
            throw new BusinessException("当前信用等级暂不支持纯信用贷款，建议补充担保材料后申请抵押类产品");
        }

        LocalDateTime now = now();
        LoanApplication entity = new LoanApplication();
        entity.setId(generateApplyId());
        entity.setProduct(product);
        entity.setProductName(product.getName());
        entity.setApplicant(user);
        entity.setApplicantName(request.getName());
        entity.setPhone(request.getPhone());
        entity.setIdCard(maskIdCard(request.getIdCard()));
        entity.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setTerm(request.getTerm());
        entity.setPurpose(request.getPurpose());
        entity.setRepaySource(request.getRepaySource());
        entity.setAddress(request.getAddress());
        entity.setBusiness(request.getBusiness());
        entity.setScale(request.getScale());
        entity.setCreditScore(user.getCreditScore());
        entity.setStatus(ApplyStatus.PENDING_FIRST);
        entity.setRate(product.getRate());
        entity.setApplyTime(now);
        entity.setUpdateTime(now);
        entity.setRepayAmount(BigDecimal.ZERO);
        entity.addLog("提交申请", request.getName(), "线上提交，等待金融机构初审");
        applicationRepository.save(entity);

        // 产品申请次数 +1
        product.setApplyCount(product.getApplyCount() + 1);
        productRepository.save(product);

        return dtoMapper.toApplicationDTO(entity);
    }

    /** 撤回（删除）：仅申请人本人、且仍处于「待初审」的申请可撤回 */
    @OpLog(module = "贷款申请", action = "撤回贷款申请", target = "#id")
    @Transactional
    public String delete(String id) {
        LoanApplication entity = loadAndCheckOwner(id);
        requireStatus(entity, ApplyStatus.PENDING_FIRST, "仅待初审的申请可以撤回，已进入审批流程的申请无法删除");

        // 回滚产品的申请次数，保持统计口径一致
        Product product = entity.getProduct();
        if (product != null && product.getApplyCount() > 0) {
            product.setApplyCount(product.getApplyCount() - 1);
            productRepository.save(product);
        }
        applicationRepository.delete(entity);
        return id;
    }

    /** 签约：初审通过 → 已签约 */
    @OpLog(module = "贷款申请", action = "签署借款合同", target = "#id")
    @Transactional
    public ApplicationDTO sign(String id) {
        LoanApplication entity = loadAndCheckOwner(id);
        requireStatus(entity, ApplyStatus.FIRST_PASS, "仅初审通过的申请可以签约");
        LocalDateTime now = now();
        entity.setStatus(ApplyStatus.SIGNED);
        entity.setSignedTime(now);
        entity.setUpdateTime(now);
        entity.addLog("签署合同", entity.getApplicantName(), "线上签署借款合同");
        return dtoMapper.toApplicationDTO(applicationRepository.save(entity));
    }

    /** 放款：已签约 → 已放款，资金进入账户余额 */
    @OpLog(module = "贷款申请", action = "贷款放款", target = "#id")
    @Transactional
    public ApplicationDTO loan(String id) {
        LoanApplication entity = loadAndCheckOwner(id);
        requireStatus(entity, ApplyStatus.SIGNED, "仅已签约的申请可以放款");
        LocalDateTime now = now();
        entity.setStatus(ApplyStatus.LOANED);
        entity.setLoanTime(now);
        entity.setUpdateTime(now);
        entity.addLog("放款成功", "系统", "贷款资金已发放至借款人账户");

        User user = entity.getApplicant();
        user.setBalance(user.getBalance().add(entity.getAmount()).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);
        return dtoMapper.toApplicationDTO(applicationRepository.save(entity));
    }

    /** 结清：已放款 → 已结清，扣本息、信用积分 +5 */
    @OpLog(module = "贷款申请", action = "归还贷款结清", target = "#id")
    @Transactional
    public ApplicationDTO settle(String id) {
        LoanApplication entity = loadAndCheckOwner(id);
        requireStatus(entity, ApplyStatus.LOANED, "仅已放款的申请可以办理结清");

        BigDecimal total = dtoMapper.repayTotal(entity);
        User user = entity.getApplicant();
        if (user.getBalance().compareTo(total) < 0) {
            throw new BusinessException(String.format(
                    "账户余额不足：应还本息 %,.2f 元，当前余额 %,.2f 元，请先充值",
                    total, user.getBalance()));
        }
        LocalDateTime now = now();
        user.setBalance(user.getBalance().subtract(total).setScale(2, RoundingMode.HALF_UP));
        // 信用积分 +5，履约能力 +1（自适应等级随之变化）
        user.setCreditScore(user.getCreditScore() + 5);
        user.setRepayScore(Math.min(100, user.getRepayScore() + 1));
        userRepository.save(user);

        entity.setStatus(ApplyStatus.SETTLED);
        entity.setSettleTime(now);
        entity.setRepayAmount(total);
        entity.setUpdateTime(now);
        entity.addLog("贷款结清", entity.getApplicantName(), "归还本息 " + total.setScale(2, RoundingMode.HALF_UP) + " 元");
        applicationRepository.save(entity);

        CreditLog creditLog = new CreditLog();
        creditLog.setId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        creditLog.setUserId(user.getId());
        creditLog.setDate(LocalDate.now().toString());
        creditLog.setEvent("贷款按时结清（" + entity.getProductName() + "）");
        creditLog.setDelta(5);
        creditLog.setScore(user.getCreditScore());
        creditLog.setType("repay");
        creditLogRepository.save(creditLog);

        return dtoMapper.toApplicationDTO(entity);
    }

    /** 账户充值（演示用，便于构造还款场景） */
    @OpLog(module = "账户", action = "账户充值", target = "#amount")
    @Transactional
    public BigDecimal recharge(BigDecimal amount) {
        User user = requireApplicant();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于 0");
        }
        if (amount.compareTo(MAX_RECHARGE) > 0) {
            throw new BusinessException("单次充值不能超过 1,000,000 元");
        }
        user.setBalance(user.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);
        return user.getBalance();
    }

    /* ==================== 审批侧操作 ==================== */

    /** 初审：通过 → 初审通过；拒绝 → 已拒绝（终态） */
    @OpLog(module = "审批", action = "贷款初审", target = "#id")
    @Transactional
    public ApplicationDTO audit(String id, AuditRequest request) {
        LoanApplication entity = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请单不存在"));
        requireStatus(entity, ApplyStatus.PENDING_FIRST, "仅待初审的申请可以审批");
        if (entity.getApplicant().getId().equals(SecurityUtils.currentUserId())) {
            // 理论上审批员不会是申请人（角色隔离），此处为双保险
            throw new BusinessException(403, "不能审批自己提交的申请");
        }
        boolean pass = "pass".equals(request.getAction());
        LoginUser auditor = SecurityUtils.require();
        LocalDateTime now = now();
        entity.setStatus(pass ? ApplyStatus.FIRST_PASS : ApplyStatus.REJECTED);
        entity.setAuditTime(now);
        entity.setAuditOpinion(request.getOpinion());
        entity.setAuditor(auditor.getName());
        entity.setUpdateTime(now);
        entity.addLog(pass ? "初审通过" : "已拒绝", auditor.getName(), request.getOpinion());
        return dtoMapper.toApplicationDTO(applicationRepository.save(entity));
    }

    /* ==================== 内部工具 ==================== */

    private User requireApplicant() {
        User user = currentUser();
        if (user.getRole() == Role.BANK_ADMIN) {
            throw new BusinessException(403, "审批人员账号不参与贷款申请");
        }
        return user;
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessException(401, "登录状态已失效，请重新登录"));
    }

    /** 加载申请单并校验数据权限：本人或审批员（审批员仅可查看，不可代签/放款/结清） */
    private LoanApplication loadAndCheckPermission(String id) {
        LoanApplication entity = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请单不存在"));
        if (!SecurityUtils.isBankAdmin()
                && !entity.getApplicant().getId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(403, "无权查看或操作他人的申请单");
        }
        return entity;
    }

    /** 仅申请人本人可操作的场景（签约 / 放款 / 结清） */
    private LoanApplication loadAndCheckOwner(String id) {
        LoanApplication entity = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请单不存在"));
        if (SecurityUtils.isBankAdmin()
                || !entity.getApplicant().getId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(403, "仅申请人本人可执行该操作");
        }
        return entity;
    }

    private void requireStatus(LoanApplication entity, ApplyStatus expected, String message) {
        if (entity.getStatus() != expected) {
            throw new BusinessException(message + "（当前状态：" + entity.getStatus().getLabel() + "）");
        }
    }

    /** 申请编号：SQ + yyyyMMdd + 3 位当日序号（冲突则继续自增） */
    private String generateApplyId() {
        String day = LocalDate.now().format(DAY);
        for (int i = 1; i <= 999; i++) {
            String id = "SQ" + day + String.format("%03d", i);
            if (!applicationRepository.existsById(id)) {
                return id;
            }
        }
        return "SQ" + day + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    private LocalDateTime now() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "**********" + idCard.substring(idCard.length() - 4);
    }
}
