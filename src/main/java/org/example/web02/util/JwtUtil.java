package org.example.web02.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类
 * 负责 JWT 令牌的生成、解析和验证
 * 使用 HMAC-SHA256 算法签名
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥（从配置读取） */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT 令牌有效期（毫秒，从配置读取） */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     * 将配置的字符串密钥转换为 SecretKey 对象
     *
     * @return HMAC-SHA256 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从令牌中提取用户ID
     *
     * @param token JWT 令牌
     * @return 用户ID（存储在 subject 字段中）
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中提取过期时间
     *
     * @param token JWT 令牌
     * @return 令牌过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从令牌中提取指定声明
     *
     * @param token JWT 令牌
     * @param claimsResolver 声明提取函数
     * @param <T> 返回值类型
     * @return 提取的声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取所有声明
     *
     * @param token JWT 令牌
     * @return 令牌中包含的所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 判断令牌是否已过期
     *
     * @param token JWT 令牌
     * @return true 表示已过期，false 表示未过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成 JWT 令牌
     * 将用户ID和角色信息封装到令牌中
     *
     * @param userId 用户ID
     * @param role 用户角色（0=普通用户，1=管理员）
     * @return 生成的 JWT 令牌字符串
     */
    public String generateToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, userId.toString());
    }

    /**
     * 创建 JWT 令牌
     * 设置令牌的主题、签发时间、过期时间，并使用签名密钥签名
     *
     * @param claims 自定义声明
     * @param subject 令牌主题（用户ID）
     * @return 签名的 JWT 令牌字符串
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证令牌是否有效且属于指定用户
     * 校验用户ID是否匹配且令牌未过期
     *
     * @param token JWT 令牌
     * @param userId 期望的用户ID
     * @return true 表示令牌有效且属于该用户
     */
    public Boolean validateToken(String token, Long userId) {
        try {
            final String extractedUserId = extractUserId(token);
            return (extractedUserId.equals(userId.toString())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查令牌是否有效（仅验证签名和过期时间，不校验用户ID）
     *
     * @param token JWT 令牌
     * @return true 表示令牌签名正确且未过期
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中提取用户角色
     *
     * @param token JWT 令牌
     * @return 用户角色编码（0=普通用户，1=管理员），提取失败返回 null
     */
    public Integer extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", Integer.class));
    }
}