package com.fwwb.vehicledetection.controller.vehicle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // 车辆列表获取
    @GetMapping("/{pageNum}")
    public Page<Vehicle> listVehicles(@PathVariable int pageNum) {
        return vehicleService.page(new Page<>(pageNum, 10));
    }

    // 获取车辆信息
    @GetMapping("/info/{vehicleId}")
    public Vehicle getVehicle(@PathVariable Long vehicleId) {
        return vehicleService.getById(vehicleId);
    }

    // 搜索车辆（根据 licence）
    @GetMapping("/licence")
    public Page<Vehicle> searchVehicles(@RequestParam("licence") String keyword,
                                        @RequestParam("pageNum") int pageNum) {
        QueryWrapper<Vehicle> query = new QueryWrapper<>();
        query.like("licence", keyword);
        return vehicleService.page(new Page<>(pageNum, 10), query);
    }

    // 车辆信息更新
    @PutMapping("/{vehicleId}")
    public String updateVehicle(@PathVariable Long vehicleId, @RequestBody Vehicle vehicle) {
        vehicle.setVehicleId(vehicleId);
        return vehicleService.updateById(vehicle) ? "更新成功" : "更新失败";
    }

    // 车辆删除
    @DeleteMapping("/{vehicleId}")
    public String deleteVehicle(@PathVariable Long vehicleId) {
        return vehicleService.removeById(vehicleId) ? "删除成功" : "删除失败";
    }

    // 车辆添加
    @PostMapping
    public String addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.save(vehicle) ? "添加成功" : "添加失败";
    }

    // 新增接口：获取数据库中的车辆总页数
    // 可以通过请求参数 pageSize 指定每页记录数，默认值为10
    @GetMapping("/pageCount")
    public int getTotalPageCount(@RequestParam(defaultValue = "10") int pageSize) {
        long totalRecords = vehicleService.count();
        // 向上取整计算总页数： (totalRecords + pageSize - 1) / pageSize
        return (int) ((totalRecords + pageSize - 1) / pageSize);
    }
}