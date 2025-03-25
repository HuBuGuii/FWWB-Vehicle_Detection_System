// File: src/main/java/com/fwwb/vehicledetection/controller/detection/NonRealTimeDetectionController.java
package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fwwb.vehicledetection.domain.dto.NonRealTimeSearchDTO;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/detections/nonrealtime")
public class NonRealTimeDetectionController {

    @Autowired
    private NonRealTimeDetectionRecordService nonRealTimeService;

    // 获取非实时检测记录（分页）
    @GetMapping("/{pageNum}")
    public Page<NonRealTimeDetectionRecord> listRecords(@PathVariable int pageNum) {
        return nonRealTimeService.page(new Page<>(pageNum, 10));
    }

    // 根据类型获取非实时记录（示例：根据 vehicleStatus 筛选）
    @GetMapping("/type")
    public Page<NonRealTimeDetectionRecord> searchRecords(@RequestParam("type") String type,
                                                          @RequestParam("pageNum") int pageNum) {
        return nonRealTimeService.page(new Page<>(pageNum, 10),
                new QueryWrapper<NonRealTimeDetectionRecord>().eq("vehicle_status", type));
    }

    /**
     * 多条件筛选非实时检测记录
     * @param searchDTO 包含筛选条件的DTO对象
     * @param pageNum 页码
     * @return 分页查询结果
     */
    @GetMapping("/search")
    public Page<NonRealTimeDetectionRecord> searchNonRealTimeRecords(
            @RequestBody NonRealTimeSearchDTO searchDTO,
            @RequestParam(defaultValue = "1") int pageNum) {

        QueryWrapper<NonRealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

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

        return nonRealTimeService.page(new Page<>(pageNum, 10), queryWrapper);
    }

    // 创建非实时检测记录
    @PostMapping
    public String addRecord(@RequestBody NonRealTimeDetectionRecord record) {
        return nonRealTimeService.save(record) ? "添加成功" : "添加失败";
    }

    // 更新非实时检测记录
    @PutMapping("/{nrdId}")
    public String updateRecord(@PathVariable Long nrdId, @RequestBody NonRealTimeDetectionRecord record) {
        record.setNrdId(nrdId);
        return nonRealTimeService.updateById(record) ? "更新成功" : "更新失败";
    }

    // 删除非实时检测记录
    @DeleteMapping("/{nrdId}")
    public String deleteRecord(@PathVariable Long nrdId) {
        return nonRealTimeService.removeById(nrdId) ? "删除成功" : "删除失败";
    }
}