package com.agriloan.service;

import com.agriloan.aop.OpLog;
import com.agriloan.common.BusinessException;
import com.agriloan.domain.CreditLog;
import com.agriloan.domain.Role;
import com.agriloan.domain.User;
import com.agriloan.dto.UserDTO;
import com.agriloan.dto.UserSaveRequest;
import com.agriloan.repository.CreditLogRepository;
import com.agriloan.repository.UserRepository;
import com.agriloan.security.LoginUser;
import com.agriloan.security.SecurityUtils;
import com.agriloan.support.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 用户管理（审批人员专用）：用户列表、人工调整信用积分、开户与资料维护
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final CreditLogRepository creditLogRepository;

    private final DtoMapper dtoMapper;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> listAll() {
        return userRepository.findAllByOrderByIdAsc().stream().map(dtoMapper::toUserDTO).toList();
    }

    /* ==================== 新增 / 修改 ==================== */

    /** 管理员开户：可开通农户 / 农业企业 / 审批人员，并直接设定初始信用档案 */
    @OpLog(module = "用户管理", action = "新增系统账号", target = "#request.username")
    @Transactional
    public UserDTO create(UserSaveRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            throw new BusinessException("请填写登录账号");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("账号已存在，请更换后重试");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("请填写登录密码");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("请输入姓名或主体名称");
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new BusinessException("请选择角色");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        fill(user, request);
        userRepository.save(user);

        // 建立初始信用档案，保证信用页有据可查
        CreditLog log = new CreditLog();
        log.setId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        log.setUserId(user.getId());
        log.setDate(LocalDate.now().toString());
        log.setEvent("管理员开户并建立涉农信用档案");
        log.setDelta(user.getCreditScore());
        log.setScore(user.getCreditScore());
        log.setType("record");
        creditLogRepository.save(log);

        return dtoMapper.toUserDTO(user);
    }

    /** 修改用户：账号不可改；password 留空表示不改密码；未传字段保持原值 */
    @OpLog(module = "用户管理", action = "修改系统账号", target = "#id")
    @Transactional
    public UserDTO update(Long id, UserSaveRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        LoginUser loginUser = SecurityUtils.current();
        if (loginUser != null && id.equals(loginUser.getUserId())) {
            if (request.getEnabled() != null && !request.getEnabled()) {
                throw new BusinessException("不能停用当前登录的管理员账号");
            }
            if (request.getRole() != null && parseRole(request.getRole()) != Role.BANK_ADMIN) {
                throw new BusinessException("不能修改当前登录账号的角色");
            }
        }

        int beforeScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        fill(user, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        // 信用积分被直接改写时补一条变动日志，与"人工调整积分"口径一致
        int delta = user.getCreditScore() - beforeScore;
        if (delta != 0) {
            CreditLog log = new CreditLog();
            log.setId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            log.setUserId(user.getId());
            log.setDate(LocalDate.now().toString());
            log.setEvent("管理员修改信用积分");
            log.setDelta(delta);
            log.setScore(user.getCreditScore());
            log.setType("record");
            creditLogRepository.save(log);
        }

        return dtoMapper.toUserDTO(user);
    }

    /** 请求参数 -> 实体（只覆盖请求中出现的字段，传空字符串可清空文本字段） */
    private void fill(User user, UserSaveRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(parseRole(request.getRole()));
        }
        if (request.getPhone() != null) {
            user.setPhone(trimToNull(request.getPhone()));
        }
        if (request.getIdCard() != null) {
            user.setIdCard(trimToNull(request.getIdCard()));
        }
        if (request.getAddress() != null) {
            user.setAddress(trimToNull(request.getAddress()));
        }
        if (request.getBusiness() != null) {
            user.setBusiness(trimToNull(request.getBusiness()));
        }
        if (request.getScale() != null) {
            user.setScale(trimToNull(request.getScale()));
        }
        if (request.getCreditScore() != null) {
            user.setCreditScore(request.getCreditScore());
        }
        if (request.getBalance() != null) {
            user.setBalance(request.getBalance());
        }
        if (request.getRepayScore() != null) {
            user.setRepayScore(request.getRepayScore());
        }
        if (request.getBusinessScore() != null) {
            user.setBusinessScore(request.getBusinessScore());
        }
        if (request.getAssetScore() != null) {
            user.setAssetScore(request.getAssetScore());
        }
        if (request.getRecordScore() != null) {
            user.setRecordScore(request.getRecordScore());
        }
        if (request.getPolicyScore() != null) {
            user.setPolicyScore(request.getPolicyScore());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
    }

    private Role parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("请选择角色");
        }
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value.trim()) || role.getLabel().equals(value.trim())) {
                return role;
            }
        }
        throw new BusinessException("未知角色：" + value);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** 人工调整信用积分（风控线下核实后录入），自动写入积分变动日志 */
    @OpLog(module = "信用", action = "人工调整信用积分", target = "#userId")
    @Transactional
    public UserDTO adjustCredit(Long userId, int delta, String event, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        int score = Math.max(CreditPolicyService.SCORE_MIN,
                Math.min(CreditPolicyService.SCORE_MAX, user.getCreditScore() + delta));
        user.setCreditScore(score);
        userRepository.save(user);

        CreditLog log = new CreditLog();
        log.setId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        log.setUserId(userId);
        log.setDate(LocalDate.now().toString());
        log.setEvent(event == null || event.isBlank() ? "风控人工核定" : event);
        log.setDelta(delta);
        log.setScore(score);
        log.setType(type == null || type.isBlank() ? "record" : type);
        creditLogRepository.save(log);

        return dtoMapper.toUserDTO(user);
    }
}
