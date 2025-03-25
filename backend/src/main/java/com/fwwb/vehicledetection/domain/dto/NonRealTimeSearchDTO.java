// File: src/main/java/com/fwwb/vehicledetection/domain/dto/NonRealTimeSearchDTO.java
package com.fwwb.vehicledetection.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NonRealTimeSearchDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String license;
    private String type;
}