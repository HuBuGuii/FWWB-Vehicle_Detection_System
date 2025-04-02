package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/detections/realtime")
public class RealTimeDetectionController {

    @Autowired
    private RealTimeDetectionRecordService realTimeService;

    // 获取实时检测记录（分页）
    @GetMapping("/{pageNum}")
    public Page<RealTimeDetectionRecord> listRecords(@PathVariable int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 13));
    }

    // 根据类型获取实时记录（示例：根据 vehicleStatus 筛选）
    @GetMapping("/type")
    public Page<RealTimeDetectionRecord> searchRecords(@RequestParam("type") String type,
                                                       @RequestParam("pageNum") int pageNum) {
        return realTimeService.page(new Page<>(pageNum, 13),
                new QueryWrapper<RealTimeDetectionRecord>().eq("vehicle_status", type));
    }

    /**
     * 多条件筛选实时检测记录
     *
     * 改为通过 URL 参数传递筛选条件：
     * - startTime 与 endTime：使用 ISO 日期时间格式（例如：2025-04-01T10:15:30）
     * - type：车辆状态
     * - license：车牌号
     * - location：摄像头位置
     * - pageNum：页码（默认为1）
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param type      车辆状态（可选）
     * @param license   车牌号（可选）
     * @param location  摄像头位置（可选）
     * @param pageNum   页码
     * @return 分页查询结果
     */
    @GetMapping("/search")
    public Page<RealTimeDetectionRecord> searchRealTimeRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String license,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") int pageNum) {

        QueryWrapper<RealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

        // 时间范围条件
        if (startTime != null && endTime != null) {
            queryWrapper.between("time", startTime, endTime);
        } else if (startTime != null) {
            queryWrapper.ge("time", startTime);
        } else if (endTime != null) {
            queryWrapper.le("time", endTime);
        }

        // 车辆类型条件
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq("vehicle_status", type);
        }

        // 车牌号条件（需要关联车辆表查询）
        if (license != null && !license.isEmpty()) {
            queryWrapper.inSql("vehicle_id",
                    "SELECT vehicle_id FROM vehicle WHERE licence LIKE '%" + license + "%'");
        }

        // 位置条件（需要关联摄像头表查询）
        if (location != null && !location.isEmpty()) {
            queryWrapper.inSql("camera_id",
                    "SELECT camera_id FROM camera WHERE location LIKE '%" + location + "%'");
        }

        return realTimeService.page(new Page<>(pageNum, 13), queryWrapper);
    }

    // 新增接口：获取实时检测记录的总页数
    // 前端可通过 pageSize 参数指定每页记录条数，默认值为 10
    @GetMapping("/pageCount")
    public int getTotalPageCount(@RequestParam(defaultValue = "13") int pageSize) {
        long totalRecords = realTimeService.count();
        // 向上取整计算总页数： (totalRecords + pageSize - 1) / pageSize
        return (int) ((totalRecords + pageSize - 1) / pageSize);
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