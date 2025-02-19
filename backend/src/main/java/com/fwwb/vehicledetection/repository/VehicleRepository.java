package com.fwwb.vehicledetection.repository;

import com.fwwb.vehicledetection.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // 车牌号模糊搜索
    List<Vehicle> findByLicenseContaining(String keyword);

    // 分页获取车辆表
    @Query("SELECT v FROM Vehicle v")
    Page<Vehicle> findAllVehicles(Pageable pageable);
}
