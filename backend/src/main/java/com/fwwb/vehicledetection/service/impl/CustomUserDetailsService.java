// File: src/main/java/com/fwwb/vehicledetection/service/impl/CustomUserDetailsService.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fwwb.vehicledetection.domain.model.User;
import com.fwwb.vehicledetection.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("account", username);
        User user = userService.getOne(query);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // 将 roleId==2 的用户认证为管理员，其余用户为普通用户
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoleId() != null && user.getRoleId() == 2) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        // 返回 Spring Security 内置的 UserDetails 实现
        return new org.springframework.security.core.userdetails.User(
                user.getAccount(), user.getPassword(), authorities);
    }
}