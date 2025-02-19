package com.fwwb.vehicledetection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicleId")
    private int vehicleId;

    @Column(name = "licence")
    private String licence;

    @Column(name = "type")
    private String type;
}
