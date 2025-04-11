package com.fwwb.vehicledetection.controller.detection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/detections/nonrealtime")
public class NonRealTimeDetectionController {

    @Autowired
    private NonRealTimeDetectionRecordService nonRealTimeService;

    // 获取非实时检测记录（分页）
    @GetMapping("/{pageNum}")
    public Page<NonRealTimeDetectionRecord> listRecords(@PathVariable int pageNum) {
        return nonRealTimeService.page(new Page<>(pageNum, 13));
    }

    /**
     * 新增接口：获取数据库中非实时检测记录的总页数
     * 可以通过请求参数 pageSize 指定每页记录数，默认值为 13
     * URL 示例: /api/detections/nonrealtime/pageCount?pageSize=13
     */
    @GetMapping("/pageCount")
    public int getTotalPageCount(@RequestParam(defaultValue = "13") int pageSize) {
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
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param type      车辆类型（可选）  —— 对应车辆表中的 type 字段
     * @param license   车牌号（可选）  —— 对应车辆表中的 licence 字段
     * @param pageNum   页码
     * @return 分页查询结果，同时包含总页数信息
     */
    @GetMapping("/search")
    public Page<NonRealTimeDetectionRecord> searchNonRealTimeRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String license,
            @RequestParam(defaultValue = "1") int pageNum) {

        int pageSize = 13;
        QueryWrapper<NonRealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

        // 时间范围条件
        if (startTime != null && endTime != null) {
            queryWrapper.between("time", startTime, endTime);
        } else if (startTime != null) {
            queryWrapper.ge("time", startTime);
        } else if (endTime != null) {
            queryWrapper.le("time", endTime);
        }

        // 车辆类型和车牌号条件
        if ((type != null && !type.trim().isEmpty()) || (license != null && !license.trim().isEmpty())) {
            StringBuilder subQuery = new StringBuilder("vehicle_id IN (SELECT vehicle_id FROM vehicle WHERE 1=1");
            List<Object> params = new ArrayList<>();
            int paramIndex = 0;

            if (type != null && !type.trim().isEmpty()) {
                subQuery.append(" AND type = {" + paramIndex + "}");
                params.add(type);
                paramIndex++;
            }
            if (license != null && !license.trim().isEmpty()) {
                subQuery.append(" AND licence LIKE CONCAT('%', {" + paramIndex + "}, '%')");
                params.add(license);
                paramIndex++;
            }
            subQuery.append(")");

            log.info("Generated subQuery: {}, params: {}", subQuery, params);
            queryWrapper.apply(subQuery.toString(), params.toArray());
        }

        try {
            log.info("Executing query with params: pageNum={}, pageSize={}, type={}, license={}", pageNum, pageSize, type, license);
            Page<NonRealTimeDetectionRecord> page = nonRealTimeService.page(new Page<>(pageNum, pageSize), queryWrapper);
            log.info("Query result: total={}, pages={}", page.getTotal(), page.getPages());
            return page;
        } catch (Exception e) {
            log.error("查询非实时检测记录失败，queryWrapper={}", queryWrapper.toString(), e);
            throw new RuntimeException("查询失败，详细信息: " + e.toString(), e);
        }
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