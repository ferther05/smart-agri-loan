package com.agriloan.service;

import com.agriloan.aop.OpLog;
import com.agriloan.common.BusinessException;
import com.agriloan.domain.CreditLog;
import com.agriloan.domain.Role;
import com.agriloan.domain.User;
import com.agriloan.dto.AuthResponse;
import com.agriloan.dto.LoginRequest;
import com.agriloan.dto.RegisterRequest;
import com.agriloan.dto.UserDTO;
import com.agriloan.repository.CreditLogRepository;
import com.agriloan.repository.UserRepository;
import com.agriloan.security.JwtService;
import com.agriloan.security.LoginUser;
import com.agriloan.security.SecurityUtils;
import com.agriloan.support.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 认证服务：注册 / 登录 / 获取当前用户
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final CreditLogRepository creditLogRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final DtoMapper dtoMapper;

    /**
     * 注册：农户 / 农业企业自助注册；审批人员不允许自行注册（由后台初始化）
     */
    @OpLog(module = "认证", action = "注册账号", target = "#request.username")
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("账号已存在，请更换后重试");
        }
        Role role = parseRegisterRole(request.getRole());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(role);
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setBusiness(request.getBusiness());
        user.setScale(request.getScale());
        user.setBalance(BigDecimal.ZERO);

        // 新用户初始信用档案：企业与农户起点不同（体现"不同人员自适应"）
        boolean enterprise = role == Role.ENTERPRISE;
        user.setCreditScore(enterprise ? 720 : 660);
        user.setRepayScore(enterprise ? 70 : 62);
        user.setBusinessScore(enterprise ? 75 : 60);
        user.setAssetScore(enterprise ? 78 : 58);
        user.setRecordScore(enterprise ? 72 : 64);
        user.setPolicyScore(enterprise ? 74 : 62);
        userRepository.save(user);

        // 建立初始积分档案记录
        CreditLog log = new CreditLog();
        log.setId("C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        log.setUserId(user.getId());
        log.setDate(LocalDate.now().toString());
        log.setEvent("首次建立涉农信用档案");
        log.setDelta(user.getCreditScore());
        log.setScore(user.getCreditScore());
        log.setType("record");
        creditLogRepository.save(log);

        return buildAuthResponse(user);
    }

    /** 登录：Spring Security 校验密码，成功签发 JWT */
    @OpLog(module = "认证", action = "账号登录", target = "#request.username")
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new BusinessException(401, "账号或密码错误");
        }
        // 写入上下文，便于操作日志记录真实操作人
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "账号不存在"));
        return buildAuthResponse(user);
    }

    /** 当前登录人信息（前端每次进入页面刷新用户态） */
    public UserDTO currentUser() {
        LoginUser loginUser = SecurityUtils.require();
        User user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(401, "账号不存在"));
        return dtoMapper.toUserDTO(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generate(user);
        return new AuthResponse(token, jwtService.getExpiresInSeconds(), dtoMapper.toUserDTO(user));
    }

    private Role parseRegisterRole(String value) {
        if (value == null || value.isBlank()) {
            return Role.FARMER;
        }
        Role role = Role.of(value);
        if (role == Role.BANK_ADMIN) {
            throw new BusinessException("审批人员账号不开放注册，请联系管理员开通");
        }
        return role;
    }
}
