package com.fwwb.vehicledetection.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    // 使用安全的密钥生成方式（推荐使用至少 256 位密钥）
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("YourSecretKeyHereWithAtLeast32BytesLength".getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 3600000 * 24;

    // 统一 Token 生成方法
    public static String generateToken(String account, Long roleId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(account) // 新 API：setSubject -> subject
                .claim("roleId", roleId)
                .issuedAt(now) // 新 API：setIssuedAt -> issuedAt
                .expiration(expiryDate) // 新 API：setExpiration -> expiration
                .signWith(SECRET_KEY, Jwts.SIG.HS256) // 新 API：明确指定算法
                .compact();
    }

    // Token 解析方法（适配 JJWT 0.12.x）
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY) // 新 API：setSigningKey -> verifyWith
                .build()
                .parseSignedClaims(token) // 新 API：parseClaimsJws -> parseSignedClaims
                .getPayload(); // 新 API：getBody -> getPayload
    }

    // Token 验证方法
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            // Token 已过期
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            // 无效 Token 格式
            return false;
        } catch (IllegalArgumentException e) {
            // 空 Token
            return false;
        }
    }
}