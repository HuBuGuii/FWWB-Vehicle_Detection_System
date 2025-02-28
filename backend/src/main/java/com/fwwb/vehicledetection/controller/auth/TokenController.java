package com.fwwb.vehicledetection.controller.auth;

import com.fwwb.vehicledetection.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private TokenUtil tokenUtil;

    @GetMapping("/generate")
    public ResponseEntity<String> generateToken() {
        // 生成唯一 token，例如使用 UUID
        String token = UUID.randomUUID().toString();
        // 将 token 预存于 Redis，并设置相应的过期时间
        tokenUtil.storeToken(token, 3600);
        return ResponseEntity.ok(token);
    }
}