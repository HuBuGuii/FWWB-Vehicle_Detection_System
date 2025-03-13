// File: src/main/java/com/fwwb/vehicledetection/controller/detection/DetectionController.java
package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    @Autowired
    private RealTimeDetectionRecordService realTimeService;

    @Autowired
    private NonRealTimeDetectionRecordService nonRealTimeService;

    // 获取实时检测记录（分页）
    @GetMapping("/realtime/{pageNum}")
    public Page<RealTimeDetectionRecord> listRealTimeRecords(@PathVariable int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 10));
    }

    // 根据类型获取实时记录（示例：根据 vehicleStatus 筛选）
    @GetMapping("/realtime/type")
    public Page<RealTimeDetectionRecord> searchRealTimeRecords(@RequestParam("type") String type,
                                                               @RequestParam("pageNum") int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 10),
                new QueryWrapper<RealTimeDetectionRecord>().eq("vehicle_status", type));
    }

    // 创建实时检测记录
    @PostMapping("/realtime")
    public String addRealTimeRecord(@RequestBody RealTimeDetectionRecord record) {
        return realTimeService.save(record) ? "添加成功" : "添加失败";
    }

    // 更新实时检测记录
    @PutMapping("/realtime/{rdId}")
    public String updateRealTimeRecord(@PathVariable Long rdId, @RequestBody RealTimeDetectionRecord record) {
        record.setRdId(rdId);
        return realTimeService.updateById(record) ? "更新成功" : "更新失败";
    }

    // 删除实时检测记录
    @DeleteMapping("/realtime/{rdId}")
    public String deleteRealTimeRecord(@PathVariable Long rdId) {
        return realTimeService.removeById(rdId) ? "删除成功" : "删除失败";
    }

    // 获取非实时检测记录（分页）
    @GetMapping("/nonrealtime/{pageNum}")
    public Page<NonRealTimeDetectionRecord> listNonRealTimeRecords(@PathVariable int pageNum) {
        return nonRealTimeService.page(new Page<>(pageNum, 10));
    }

    // 根据类型获取非实时记录（示例：根据 vehicleStatus 筛选）
    @GetMapping("/nonrealtime/type")
    public Page<NonRealTimeDetectionRecord> searchNonRealTimeRecords(@RequestParam("type") String type,
                                                                     @RequestParam("pageNum") int pageNum) {
        return nonRealTimeService.page(new Page<>(pageNum, 10),
                new QueryWrapper<NonRealTimeDetectionRecord>().eq("vehicle_status", type));
    }

    // 创建非实时检测记录
    @PostMapping("/nonrealtime")
    public String addNonRealTimeRecord(@RequestBody NonRealTimeDetectionRecord record) {
        return nonRealTimeService.save(record) ? "添加成功" : "添加失败";
    }

    // 更新非实时检测记录
    @PutMapping("/nonrealtime/{nrdId}")
    public String updateNonRealTimeRecord(@PathVariable Long nrdId, @RequestBody NonRealTimeDetectionRecord record) {
        record.setNrdId(nrdId);
        return nonRealTimeService.updateById(record) ? "更新成功" : "更新失败";
    }

    // 删除非实时检测记录
    @DeleteMapping("/nonrealtime/{nrdId}")
    public String deleteNonRealTimeRecord(@PathVariable Long nrdId) {
        return nonRealTimeService.removeById(nrdId) ? "删除成功" : "删除失败";
    }
}