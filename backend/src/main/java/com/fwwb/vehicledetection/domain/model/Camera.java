// File: src/main/java/com/fwwb/vehicledetection/domain/model/Camera.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("camera")
public class Camera {
    @TableId(type = IdType.AUTO)
    private Long cameraId;

    // 摄像头名称
    private String cameraName;

    // 所在位置
    private String location;

    // 状态（正常/关闭/故障等）
    private String status;

    // IP 地址
    private String ipAddress;

    // 端口号
    private Integer port;

    // 协议（RTSP、HTTP等）
    private String protocol;

    // 来源（USB或者NETWORK）
    private String sourceType;

    //  本地设备序列号（网络摄像头无效）
    private Integer deviceId;
}