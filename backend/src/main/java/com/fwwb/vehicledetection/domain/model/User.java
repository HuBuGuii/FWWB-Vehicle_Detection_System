// File: src/main/java/com/fwwb/vehicledetection/domain/model/User.java
package com.fwwb.vehicledetection.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;

    // 真实姓名
    private String realName;

    // 账户
    private String account;

    // 密码
    private String password;

    // 联系方式
    private String contact;

    // 职位
    private String position;

    // 部门
    private String department;

    // 角色ID（普通用户/管理员）
    private Long roleId;

    // 授权状态（通过/投递中）
    private String authorizationStatus;


}