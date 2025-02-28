// File: src/main/java/com/fwwb/vehicledetection/interceptor/TokenInterceptor.java
package com.fwwb.vehicledetection.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fwwb.vehicledetection.exception.UnauthorizedException;
import com.fwwb.vehicledetection.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private TokenUtil tokenUtil;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request,
//                             HttpServletResponse response,
//                             Object handler) throws Exception {
//        // GET 请求通常认为是幂等的，跳过验证
//        if ("GET".equalsIgnoreCase(request.getMethod())) {
//            return true;
//        }
//
//        // 如果请求地址为 token 生成接口，则跳过幂等性校验
//        String uri = request.getRequestURI();
//        if (uri.startsWith("/api/token")) {
//            return true;
//        }
//
//        String token = request.getHeader("Idempotency-Token");
//        if (token == null) {
//            throw new UnauthorizedException("Missing idempotency token.");
//        }
//
//        // 仅验证 token 是否存在且只使用一次
//        boolean valid = tokenUtil.verifyToken(token);
//        if (!valid) {
//            throw new UnauthorizedException("Duplicate or invalid request token.");
//        }
//        return true;
//    }
}