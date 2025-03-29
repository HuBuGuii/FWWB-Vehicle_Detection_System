package com.fwwb.vehicledetection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 如果需要允许凭证，不能使用 "*"，建议明确指定允许的域名（比如 http://localhost:5173），或者使用 allowedOriginPatterns：
                .allowedOriginPatterns("*")
                // 针对允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 允许携带凭证
                .allowCredentials(true);
    }
}