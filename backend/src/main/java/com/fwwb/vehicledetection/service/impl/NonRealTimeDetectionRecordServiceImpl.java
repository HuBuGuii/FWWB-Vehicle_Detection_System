// File: src/main/java/com/fwwb/vehicledetection/service/impl/NonRealTimeDetectionRecordServiceImpl.java
package com.fwwb.vehicledetection.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.mapper.NonRealTimeDetectionRecordMapper;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;


import org.springframework.stereotype.Service;



@Service
public class NonRealTimeDetectionRecordServiceImpl extends ServiceImpl<NonRealTimeDetectionRecordMapper, NonRealTimeDetectionRecord> implements NonRealTimeDetectionRecordService {
}