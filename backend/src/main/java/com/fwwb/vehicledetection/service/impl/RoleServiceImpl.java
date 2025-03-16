// File: src/main/java/com/fwwb/vehicledetection/service/impl/RoleServiceImpl.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fwwb.vehicledetection.domain.model.Role;
import com.fwwb.vehicledetection.mapper.RoleMapper;
import com.fwwb.vehicledetection.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}