// File: src/main/java/com/fwwb/vehicledetection/domain/model/Vehicle.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vehicle")
public class Vehicle {
    @TableId(type = IdType.AUTO)
    private Long vehicleId;

    // 车牌
    private String licence;

    // 车辆类型
    private String type;
}