package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 新增接口：获取数据库中非实时检测记录的总页数
     * 可以通过请求参数 pageSize 指定每页记录数，默认值为 10
     * URL 示例: /api/detections/nonrealtime/pageCount?pageSize=10
     */
    @GetMapping("/pageCount")
    public int getTotalPageCount(@RequestParam(defaultValue = "10") int pageSize) {
        long totalRecords = nonRealTimeService.count();
        // 向上取整计算总页数： (totalRecords + pageSize - 1) / pageSize
        return (int) ((totalRecords + pageSize - 1) / pageSize);
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
     *
     * 改为通过 URL 参数传递筛选条件：
     * - startTime 与 endTime：使用 ISO 日期时间格式（例如：2025-04-01T10:15:30）
     * - type：车辆状态
     * - license：车牌号
     * - pageNum：页码（默认为1）
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param type      车辆状态（可选）
     * @param license   车牌号（可选）
     * @param pageNum   页码
     * @return 分页查询结果
     */
    @GetMapping("/search")
    public Page<NonRealTimeDetectionRecord> searchNonRealTimeRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String license,
            @RequestParam(defaultValue = "1") int pageNum) {

        QueryWrapper<NonRealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

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