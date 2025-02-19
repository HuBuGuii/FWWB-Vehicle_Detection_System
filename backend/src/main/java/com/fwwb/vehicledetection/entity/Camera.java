package com.fwwb.vehicledetection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "camera")
@Getter
@Setter
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cameraId")
    private int cameraId;

    @Column(name= "cameraName")
    private String cameraName;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status;
}
