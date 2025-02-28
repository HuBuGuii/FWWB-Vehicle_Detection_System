// File: src/main/java/com/fwwb/vehicledetection/mapper/UserMapper.java
package com.fwwb.vehicledetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fwwb.vehicledetection.domain.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}