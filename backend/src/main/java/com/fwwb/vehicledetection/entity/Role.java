package com.fwwb.vehicledetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {
    @Id
    @Column(name = "roleid")
    private int roleId;

    @Column(name = "rolename")
    private String roleName;
}
