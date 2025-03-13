// File: src/main/java/com/fwwb/vehicledetection/config/WebMvcConfig.java
package com.fwwb.vehicledetection.config;

import com.fwwb.vehicledetection.interceptor.JwtInterceptor;
import com.fwwb.vehicledetection.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 JWT 拦截器，拦截 /api/** 下的接口，排除 /api/auth/**
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");

        // 注册 Token 拦截器，拦截需要幂等性校验的接口
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/token");
    }
}