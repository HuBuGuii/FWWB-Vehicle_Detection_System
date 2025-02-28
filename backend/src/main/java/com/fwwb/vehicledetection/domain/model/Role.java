// File: src/main/java/com/fwwb/vehicledetection/domain/model/Role.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long roleId;

    // 角色名称
    private String roleName;
}