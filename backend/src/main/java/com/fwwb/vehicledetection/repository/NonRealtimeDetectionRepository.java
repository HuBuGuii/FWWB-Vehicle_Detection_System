package com.fwwb.vehicledetection.repository;

import com.fwwb.vehicledetection.entity.NonRealtimeDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface NonRealtimeDetectionRepository extends JpaRepository<NonRealtimeDetection, Long> {
    // 分页查询某个类型的非实时检测记录
    Page<NonRealtimeDetection> findByDetectionType(String detectionType, Pageable pageable);

    // 分页查询所有非实时检测记录
    @Query("SELECT n FROM NonRealtimeDetection n")
    Page<NonRealtimeDetection> findAllNonRealtimeDetections(Pageable pageable);
}
