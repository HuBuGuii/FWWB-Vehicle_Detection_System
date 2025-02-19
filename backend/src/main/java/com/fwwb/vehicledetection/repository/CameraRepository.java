package com.fwwb.vehicledetection.repository;

import com.fwwb.vehicledetection.entity.Camera;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    // 根据地理位置模糊搜索摄像头
    List<Camera> findByLocationContaining(String keyword);

    // 分页获取摄像头列表
    @Query("SELECT c FROM Camera c")
    Page<Camera> findAllCameras(Pageable pageable);
}
