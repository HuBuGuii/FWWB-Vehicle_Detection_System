// File: src/main/java/com/fwwb/vehicledetection/service/impl/VehicleServiceImpl.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.mapper.VehicleMapper;
import com.fwwb.vehicledetection.service.VehicleService;
import org.springframework.stereotype.Service;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {
}