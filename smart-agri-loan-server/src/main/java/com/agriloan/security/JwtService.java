package com.agriloan.security;

import com.agriloan.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与校验
 *
 * <p>令牌载荷：sub=登录账号，uid=用户主键，role=角色码，name=姓名</p>
 */
@Component
public class JwtService {

    private final SecretKey key;

    private final long expireMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expire-minutes:720}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60 * 1000;
    }

    /** 签发令牌 */
    public String generate(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .claim("name", user.getName())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 解析令牌；非法或过期返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpiresInSeconds() {
        return expireMillis / 1000;
    }
}
