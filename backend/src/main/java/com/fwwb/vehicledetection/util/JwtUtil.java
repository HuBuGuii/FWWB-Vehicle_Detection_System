// File: src/main/java/com/fwwb/vehicledetection/util/JwtUtil.java
package com.fwwb.vehicledetection.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "YourSecretKeyHere";
    // 令牌有效期：24小时
    private static final long EXPIRATION = 3600000*24;

    public static String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static String generateToken(String account, Long roleId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        // 使用 HS256 签名算法生成 Token，同时在 Token 中设置 subject 和 claim (roleId)
        return Jwts.builder()
                .setSubject(account)
                .claim("roleId", roleId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}