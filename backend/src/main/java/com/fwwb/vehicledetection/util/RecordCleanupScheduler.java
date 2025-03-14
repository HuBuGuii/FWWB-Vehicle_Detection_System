package com.fwwb.vehicledetection.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecordCleanupScheduler {

    @Autowired
    private NonRealTimeDetectionRecordService recordService;

    // 每60秒执行一次（cron表达式格式）
    @Scheduled(fixedRate = 60 * 1000)
    public void cleanupExpiredRecords() {
        // 构建删除条件（示例使用MySQL语法）
        QueryWrapper<NonRealTimeDetectionRecord> wrapper = new QueryWrapper<>();
        wrapper.apply("time + INTERVAL max_age HOUR < NOW()");

        // 执行删除并记录日志
        int deleted = recordService.getBaseMapper().delete(wrapper);
        System.out.println("已删除过期记录数: " + deleted);
    }
}