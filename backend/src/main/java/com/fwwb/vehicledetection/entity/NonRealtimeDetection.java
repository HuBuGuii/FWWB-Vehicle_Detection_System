package com.fwwb.vehicledetection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "nonRealTimeDetectionRecord")
@Getter
@Setter
public class NonRealtimeDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nrdId")
    private int id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;

    @Column(name = "time")
    private Date time;

    @Column(name = "confidence")
    private double confidence;

    @ManyToOne
    @JoinColumn(name = "vehicleId",referencedColumnName = "vehicleId")
    private Vehicle vehicle;

    @Column(name = "vehicleStatus")
    private String vehicleStatus;

    @Column(name = "maxAge")
    private double maxAge;
}
