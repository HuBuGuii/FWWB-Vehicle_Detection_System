// File: src/main/java/com/fwwb/vehicledetection/service/impl/CameraServiceImpl.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fwwb.vehicledetection.domain.model.Camera;
import com.fwwb.vehicledetection.mapper.CameraMapper;
import com.fwwb.vehicledetection.service.CameraService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CameraServiceImpl extends ServiceImpl<CameraMapper, Camera> implements CameraService {
}