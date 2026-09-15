package com.agriloan.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：从 Authorization: Bearer xxx 中解析用户并写入 SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIX.length()).trim();
            Claims claims = jwtService.parse(token);
            if (claims != null) {
                Long uid = claims.get("uid", Number.class) == null ? null : claims.get("uid", Number.class).longValue();
                String role = claims.get("role", String.class);
                String name = claims.get("name", String.class);
                LoginUser loginUser = new LoginUser(uid, claims.getSubject(), "", name, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        loginUser, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
