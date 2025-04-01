package com.fwwb.vehicledetection.controller.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fwwb.vehicledetection.domain.model.User;
import com.fwwb.vehicledetection.service.UserService;
import com.fwwb.vehicledetection.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户注册接口
     * 修改点：
     * 1. 默认将 roleId 设置为 1 表示普通用户
     * 2. 将密码通过 BCrypt 进行加密
     * 3. 将审批状态设置为 "in-progress"，等待管理员审核
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 设置默认角色为普通用户
        user.setRoleId(1L);
        // 设置默认审批状态为 "in-progress"
        user.setAuthorizationStatus("in-progress");
        // 对密码进行 BCrypt 加密
        String encodedPwd = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPwd);

        boolean result = userService.save(user);
        if (result) {
            return ResponseEntity.ok("注册成功，等待管理员审核");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("注册失败");
    }

    /**
     * 用户登录接口
     * 修改点：
     * 1. 通过 BCrypt 校验密码
     * 2. 验证用户审批状态（authorizationStatus）是否为 "pass"
     * 3. 通过 JwtUtil.generateToken(account, roleId) 生成包含角色信息的 token
     * 4. 返回 token 的同时，也返回用户的 userId、roleId、realName
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginMap) {
        String account = loginMap.get("account");
        String rawPassword = loginMap.get("password");

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("account", account);
        User user = userService.getOne(query);

        // 用户不存在或密码不匹配
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }

        // 验证审批状态是否为 "pass"
        if (!"pass".equalsIgnoreCase(user.getAuthorizationStatus())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未通过审核");
        }

        // 生成 JWT Token，将 account 与 roleId（用户角色）写入 token 内部
        String token = JwtUtil.generateToken(user.getAccount(), user.getRoleId());
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("userId", user.getUserId());
        resp.put("roleId", user.getRoleId());
        resp.put("realName", user.getRealName());

        return ResponseEntity.ok(resp);
    }
}