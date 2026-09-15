package com.agriloan.security;

import com.agriloan.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录人工具：业务层据此判断"这单子是不是我的""我能不能审批"
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录主体，未登录返回 null */
    public static LoginUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /** 当前登录主体，未登录抛 401 */
    public static LoginUser require() {
        LoginUser user = current();
        if (user == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return user;
    }

    public static Long currentUserId() {
        return require().getUserId();
    }

    public static String currentUsername() {
        LoginUser user = current();
        return user == null ? "匿名" : user.getUsername();
    }

    public static String currentRoleLabel() {
        LoginUser user = current();
        return user == null ? "" : user.getRoleCode();
    }

    public static boolean hasRole(String roleCode) {
        LoginUser user = current();
        return user != null && user.getRoleCode().equals(roleCode);
    }

    public static boolean isBankAdmin() {
        return hasRole("BANK_ADMIN");
    }
}
