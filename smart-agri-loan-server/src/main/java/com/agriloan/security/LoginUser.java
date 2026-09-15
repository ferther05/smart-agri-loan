package com.agriloan.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 认证主体：包装登录用户的账号、主键与角色
 */
@Getter
public class LoginUser implements UserDetails {

    private final Long userId;

    private final String username;

    private final String password;

    private final String name;

    /** 角色码，如 FARMER / ENTERPRISE / BANK_ADMIN */
    private final String roleCode;

    private final Collection<? extends GrantedAuthority> authorities;

    public LoginUser(Long userId, String username, String password, String name, String roleCode) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.roleCode = roleCode;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
