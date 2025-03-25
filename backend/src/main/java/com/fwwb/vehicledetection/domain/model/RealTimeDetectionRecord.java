// File: src/main/java/com/fwwb/vehicledetection/domain/model/RealTimeDetectionRecord.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("\"realTimeDetectionRecord\"")
public class RealTimeDetectionRecord {
    // 主键：实时监测记录ID
    @TableId(type = IdType.AUTO)
    private Long rdId;

    // 由哪个摄像头提供
    private Long cameraId;

    // 检测结果的置信度
    private Double confidence;

    // 气温
    private Double temperature;

    // 天气状况
    private String weather;

    // 记录产生时间
    private LocalDateTime time;

    // 车辆ID
    private Long vehicleId;

    // 车辆状态（进入/行驶/离开）
    private String vehicleStatus;

    // 记录的最大寿命
    private Long maxAge;

    // 记录保存的文件夹
    private String exp;
}