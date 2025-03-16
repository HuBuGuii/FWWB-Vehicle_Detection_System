// File: src/main/java/com/fwwb/vehicledetection/mapper/CameraMapper.java
package com.fwwb.vehicledetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fwwb.vehicledetection.domain.model.Camera;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CameraMapper extends BaseMapper<Camera> {
}