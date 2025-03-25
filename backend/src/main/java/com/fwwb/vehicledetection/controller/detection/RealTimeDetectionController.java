// File: src/main/java/com/fwwb/vehicledetection/controller/detection/RealTimeDetectionController.java
package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fwwb.vehicledetection.domain.dto.RealTimeSearchDTO;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/detections/realtime")
public class RealTimeDetectionController {

    @Autowired
    private RealTimeDetectionRecordService realTimeService;

    // 获取实时检测记录（分页）
    @GetMapping("/{pageNum}")
    public Page<RealTimeDetectionRecord> listRecords(@PathVariable int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 10));
    }

    // 根据类型获取实时记录（示例：根据 vehicleStatus 筛选）
    @GetMapping("/type")
    public Page<RealTimeDetectionRecord> searchRecords(@RequestParam("type") String type,
                                                       @RequestParam("pageNum") int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 10),
                new QueryWrapper<RealTimeDetectionRecord>().eq("vehicle_status", type));
    }

    /**
     * 多条件筛选实时检测记录
     * @param searchDTO 包含筛选条件的DTO对象
     * @param pageNum 页码
     * @return 分页查询结果
     */
    @GetMapping("/search")
    public Page<RealTimeDetectionRecord> searchRealTimeRecords(
            @RequestBody RealTimeSearchDTO searchDTO,
            @RequestParam(defaultValue = "1") int pageNum) {

        QueryWrapper<RealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

        // 时间范围条件
        if (searchDTO.getStartTime() != null && searchDTO.getEndTime() != null) {
            queryWrapper.between("time", searchDTO.getStartTime(), searchDTO.getEndTime());
        } else if (searchDTO.getStartTime() != null) {
            queryWrapper.ge("time", searchDTO.getStartTime());
        } else if (searchDTO.getEndTime() != null) {
            queryWrapper.le("time", searchDTO.getEndTime());
        }

        // 车辆类型条件
        if (searchDTO.getType() != null && !searchDTO.getType().isEmpty()) {
            queryWrapper.eq("vehicle_status", searchDTO.getType());
        }

        // 车牌号条件（需要关联车辆表查询）
        if (searchDTO.getLicense() != null && !searchDTO.getLicense().isEmpty()) {
            queryWrapper.inSql("vehicle_id",
                    "SELECT vehicle_id FROM vehicle WHERE licence LIKE '%" + searchDTO.getLicense() + "%'");
        }

        // 位置条件（需要关联摄像头表查询）
        if (searchDTO.getLocation() != null && !searchDTO.getLocation().isEmpty()) {
            queryWrapper.inSql("camera_id",
                    "SELECT camera_id FROM camera WHERE location LIKE '%" + searchDTO.getLocation() + "%'");
        }

        return realTimeService.page(new Page<>(pageNum, 10), queryWrapper);
    }

    // 创建实时检测记录
    @PostMapping
    public String addRecord(@RequestBody RealTimeDetectionRecord record) {
        return realTimeService.save(record) ? "添加成功" : "添加失败";
    }

    // 更新实时检测记录
    @PutMapping("/{rdId}")
    public String updateRecord(@PathVariable Long rdId, @RequestBody RealTimeDetectionRecord record) {
        record.setRdId(rdId);
        return realTimeService.updateById(record) ? "更新成功" : "更新失败";
    }

    // 删除实时检测记录
    @DeleteMapping("/{rdId}")
    public String deleteRecord(@PathVariable Long rdId) {
        return realTimeService.removeById(rdId) ? "删除成功" : "删除失败";
    }
}