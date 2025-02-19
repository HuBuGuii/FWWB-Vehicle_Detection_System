package com.fwwb.vehicledetection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name="realTimeDetectionRecord")
@Getter
@Setter
public class RealtimeDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rdId")
    private int rdId;

    @ManyToOne
    @JoinColumn(name = "cameraId", referencedColumnName = "cameraId")
    private Camera camera;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "temperature")
    private double temperature;

    @Column(name = "weather")
    private String weather;

    @Column(name = "time")
    private Date time;

    @ManyToOne
    @JoinColumn(name = "vehicleId", referencedColumnName = "vehicleId")
    private Vehicle vehicle;

    @Column(name = "vehicleStatus")
    private String vehicleStatus;

    @Column(name = "maxAge")
    private double maxAge;
}
