// File: src/main/java/com/fwwb/vehicledetection/filter/JwtAuthenticationFilter.java
package com.fwwb.vehicledetection.filter;

import com.fwwb.vehicledetection.service.impl.CustomUserDetailsService;
import com.fwwb.vehicledetection.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component  // 添加后，Spring 会管理这个过滤器
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Log logger = LogFactory.getLog(JwtAuthenticationFilter.class);

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token)) {
                // 校验 token 并获取 Claims 对象
                Claims claims = JwtUtil.parseToken(token);
                if (claims != null) {
                    // 从 token 中获取用户名（subject）
                    String username = claims.getSubject();
                    // 通过 UserDetailsService 获取用户权限信息
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // 将认证信息放入上下文中
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException e) {
            // 注意修改日志调用方式，使错误消息与异常传入正确
            logger.error("Token 已过期: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Token 解析出现异常: " + e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }

    // 从请求头中获取 token（确保前缀为 Bearer）
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}