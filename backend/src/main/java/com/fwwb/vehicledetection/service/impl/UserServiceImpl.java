// File: src/main/java/com/fwwb/vehicledetection/service/impl/UserServiceImpl.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fwwb.vehicledetection.domain.model.User;
import com.fwwb.vehicledetection.mapper.UserMapper;
import com.fwwb.vehicledetection.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}