import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS并禁用CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // 放行所有OPTIONS请求
                .requestMatchers("/api/auth/**").permitAll()             // 开放认证接口
                .anyRequest().authenticated()                            // 其他请求需认证
            );
        
        return http.build();
    }

    // CORS配置源
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));  // 精确指定允许的源
        config.setAllowedMethods(List.of("*"));                      // 允许所有HTTP方法
        config.setAllowedHeaders(List.of("*"));                      // 允许所有请求头
        config.setAllowCredentials(true);                            // 允许携带凭证
        config.setMaxAge(3600L);                                     // 预检请求缓存时间
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);             // 应用到所有路径
        return source;
    }
}