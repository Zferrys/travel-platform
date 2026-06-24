package com.travel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure JWT utility. Reads secret from environment variable JWT_SECRET or
 * system property "jwt.secret". Fails startup if missing.
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    private static final long EXPIRATION = 24 * 60 * 60 * 1000L; // 24h

    private String secret;
    private Key signingKey;

    @PostConstruct
    public void init() {
        // 优先从环境变量/系统属性读取密钥
        String prop = System.getProperty("jwt.secret");
        String env = System.getenv("JWT_SECRET");
        secret = (prop != null && !prop.isEmpty()) ? prop
               : (env != null && !env.isEmpty()) ? env
               : null;

        if (secret == null) {
            // 未配置时生成随机密钥，避免硬编码默认值被反编译后伪造 Token
            // 代价：重启后所有已签发 Token 失效（开发环境可接受）
            byte[] randomBytes = new byte[32]; // 256-bit
            new SecureRandom().nextBytes(randomBytes);
            secret = Base64.getEncoder().encodeToString(randomBytes);
            log.warn("!!! JWT_SECRET 未配置，已生成随机密钥（重启后所有 Token 失效）!!!");
            log.warn("!!! 生产环境务必设置环境变量 JWT_SECRET 或系统属性 jwt.secret !!!");
        } else {
            log.info("JWT 密钥已从环境变量/系统属性加载");
        }

        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Integer userId, String username, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        Object id = claims.get("userId");
        return id instanceof Integer ? (Integer) id : (id != null ? Integer.valueOf(String.valueOf(id)) : null);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.getSubject();
    }

    public Integer getUserTypeFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        Object t = claims.get("userType");
        return t instanceof Integer ? (Integer) t : (t != null ? Integer.valueOf(String.valueOf(t)) : null);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
