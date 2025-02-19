package com.fwwb.vehicledetection.repository;

import com.fwwb.vehicledetection.entity.RealtimeDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface RealtimeDetectionRepository extends JpaRepository<RealtimeDetection, Long> {
    // 分页查询某个类型的实时检测记录
    Page<RealtimeDetection> findByDetectionType(String detectionType, Pageable pageable);

    // 分页查询所有实时检测记录
    @Query("SELECT r FROM RealtimeDetection r")
    Page<RealtimeDetection> findAllRealtimeDetections(Pageable pageable);

}
