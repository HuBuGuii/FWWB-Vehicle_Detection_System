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
     * 多条件筛选非实时检测记录
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param type      车辆类型（可选）  —— 对应车辆表中的 type 字段
     * @param license   车牌号（可选）  —— 对应车辆表中的 licence 字段
     * @param pageNum   页码
     * @return 分页查询结果，同时包含总记录数和总页数信息
     */
    @GetMapping("/search")
    public Page<NonRealTimeDetectionRecord> searchNonRealTimeRecords(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String license,
            @RequestParam(defaultValue = "1") int pageNum) {

        // 固定每页显示 13 条记录（如果需要可以把 pageSize 作为参数传入）
        int pageSize = 13;

        QueryWrapper<NonRealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

        // 添加时间范围条件
        if (startTime != null && endTime != null) {
            queryWrapper.between("time", startTime, endTime);
        } else if (startTime != null) {
            queryWrapper.ge("time", startTime);
        } else if (endTime != null) {
            queryWrapper.le("time", endTime);
        }

        // 构造车辆表子查询：当传入车辆类型或车牌号参数时
        if ((type != null && !type.trim().isEmpty()) ||
                (license != null && !license.trim().isEmpty())) {

            StringBuilder subQuery = new StringBuilder("SELECT vehicle_id FROM vehicle WHERE 1=1");

            if (license != null && !license.trim().isEmpty()) {
                subQuery.append(" AND licence LIKE '%").append(license).append("%'");
            }

            if (type != null && !type.trim().isEmpty()) {
                subQuery.append(" AND type = '").append(type).append("'");
            }

            queryWrapper.inSql("vehicle_id", subQuery.toString());
        }

        // 使用 MyBatis-Plus 分页查询数据
        Page<NonRealTimeDetectionRecord> page = nonRealTimeService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 手动调用 count 查询，并计算总页数（公式： (totalRecords + pageSize - 1) / pageSize ）
        long totalRecords = nonRealTimeService.count(queryWrapper);
        int totalPages = (int) ((totalRecords + pageSize - 1) / pageSize);

        // 更新 Page 对象中的 total 字段，pages 字段通常为自动计算（取决于 MyBatis-Plus 版本）
        page.setTotal(totalRecords);

        // 如果需要在返回的 JSON 中明确返回自定义计算的页数，可以将 totalPages 封装到 DTO 中返回
        // 或者将其写入日志、响应头等方式传递给前端

        return page;
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