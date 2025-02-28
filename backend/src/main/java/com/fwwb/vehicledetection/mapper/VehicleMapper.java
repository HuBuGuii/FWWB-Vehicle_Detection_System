// File: src/main/java/com/fwwb/vehicledetection/mapper/VehicleMapper.java
package com.fwwb.vehicledetection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}