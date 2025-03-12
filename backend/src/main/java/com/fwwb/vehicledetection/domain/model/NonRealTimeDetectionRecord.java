// File: src/main/java/com/fwwb/vehicledetection/domain/model/NonRealTimeDetectionRecord.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("\"nonRealTimeDetectionRecord\"")
public class NonRealTimeDetectionRecord {
    // 非实时检测记录ID：主键
    @TableId(type = IdType.AUTO)
    private Long nrdId;

    // 上传用户的ID
    private Long userId;

    // 记录检测时间
    private LocalDateTime time;

    // 记录置信度
    private Double confidence;

    // 车辆ID
    private Long vehicleId;

    // 车辆状态
    private String vehicleStatus;

    // 最大寿命
    private Long maxAge;

    // exp文件夹
    private String exp;
}