// File: src/main/java/com/fwwb/vehicledetection/interceptor/JwtInterceptor.java
package com.fwwb.vehicledetection.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fwwb.vehicledetection.util.JwtUtil;
import com.fwwb.vehicledetection.exception.UnauthorizedException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        // 约定：Authorization header 格式为 "Bearer <token>"
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header.");
        }
        token = token.substring(7); // 去除 "Bearer " 前缀

        // 验证 JWT 令牌
        if (!JwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token.");
        }
        return true;
    }
}