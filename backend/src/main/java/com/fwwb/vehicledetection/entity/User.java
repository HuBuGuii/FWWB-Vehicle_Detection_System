package com.fwwb.vehicledetection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private long userId;

    @Column(name = "account")
    private String account;

    @Column(name = "password")
    private String password;

    @Column(name = "contact")
    private String contact;

    @Column(name = "position")
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "authorizationStatus")
    private String authorizationStatus;

    @ManyToOne
    @JoinColumn(name = "roleId", referencedColumnName = "roleId")
    private Role role;
}
