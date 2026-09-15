package com.agriloan.security;

import com.agriloan.domain.User;
import com.agriloan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 从数据库加载用户，供 Spring Security 认证使用
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("账号或密码错误"));
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UsernameNotFoundException("账号已被停用，请联系管理员");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getName(), user.getRole().name());
    }
}
