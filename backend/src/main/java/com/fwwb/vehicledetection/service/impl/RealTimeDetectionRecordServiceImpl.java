// File: src/main/java/com/fwwb/vehicledetection/service/impl/RealTimeDetectionRecordServiceImpl.java
package com.fwwb.vehicledetection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.mapper.RealTimeDetectionRecordMapper;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class RealTimeDetectionRecordServiceImpl extends ServiceImpl<RealTimeDetectionRecordMapper, RealTimeDetectionRecord>
        implements RealTimeDetectionRecordService {
}