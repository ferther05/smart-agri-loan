package com.agriloan.security;

import com.agriloan.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置：无状态 JWT 认证 + 基于角色的接口授权
 *
 * <p>权限矩阵：</p>
 * <ul>
 *   <li>公开：登录、注册、产品与资讯的只读查询</li>
 *   <li>农户 / 农业企业：信用积分、贷款申请（仅本人的单子）、签约 / 结清、账户充值</li>
 *   <li>审批人员：审批端（初审通过 / 拒绝）、用户管理、产品与资讯的增改、AOP 操作日志</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/news/**").permitAll()
                        // 审批端与日志管理：仅审批人员
                        .requestMatchers("/api/admin/**", "/api/oplog/**", "/api/users/**").hasRole("BANK_ADMIN")
                        .requestMatchers("/api/audit/**").hasRole("BANK_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/audit").hasRole("BANK_ADMIN")
                        // 产品与资讯的写操作（新增 / 修改 / 上下架）：仅审批人员；读操作已在上面放行
                        .requestMatchers("/api/products/**", "/api/news/**").hasRole("BANK_ADMIN")
                        // 审批人员不发起贷款
                        .requestMatchers(HttpMethod.POST, "/api/applications", "/api/account/**")
                        .hasAnyRole("FARMER", "ENTERPRISE")
                        // 撤回申请同样只对申请人（农户 / 农业企业）开放
                        .requestMatchers(HttpMethod.DELETE, "/api/applications", "/api/applications/*")
                        .hasAnyRole("FARMER", "ENTERPRISE")
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
        // 使用 originPatterns，允许 "*" 与凭证共存（本地 file:// 页面可直接调用）
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** 未登录 / token 失效：返回 401 JSON */
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录状态已失效，请重新登录");
    }

    /** 角色不足：返回 403 JSON */
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "当前角色无权执行该操作");
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
    }
}
