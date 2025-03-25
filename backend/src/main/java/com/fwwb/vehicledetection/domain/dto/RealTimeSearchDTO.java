// File: src/main/java/com/fwwb/vehicledetection/domain/dto/RealTimeSearchDTO.java
package com.fwwb.vehicledetection.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RealTimeSearchDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String license;
    private String type;
}