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
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param type      车辆类型（可选）
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

        // 固定每页显示 13 条记录（如果需要，也可将 pageSize 作为参数传入）
        int pageSize = 13;
        QueryWrapper<RealTimeDetectionRecord> queryWrapper = new QueryWrapper<>();

        // 添加时间范围条件
        if (startTime != null && endTime != null) {
            queryWrapper.between("time", startTime, endTime);
        } else if (startTime != null) {
            queryWrapper.ge("time", startTime);
        } else if (endTime != null) {
            queryWrapper.le("time", endTime);
        }

        // 如果传入车辆类型 (type) 或车牌号 (license) 参数，则构造一个关联车辆表的子查询
        if ((type != null && !type.trim().isEmpty()) || (license != null && !license.trim().isEmpty())) {
            StringBuilder vehicleSubQuery = new StringBuilder("SELECT vehicle_id FROM vehicle WHERE 1=1");
            if (license != null && !license.trim().isEmpty()) {
                vehicleSubQuery.append(" AND licence LIKE '%").append(license.trim()).append("%'");
            }
            if (type != null && !type.trim().isEmpty()) {
                vehicleSubQuery.append(" AND type = '").append(type.trim()).append("'");
            }
            queryWrapper.inSql("vehicle_id", vehicleSubQuery.toString());
        }

        // 根据摄像头位置 (location) 筛选，通过关联摄像头表查询
        if (location != null && !location.trim().isEmpty()) {
            StringBuilder cameraSubQuery = new StringBuilder("SELECT camera_id FROM camera WHERE location LIKE '%")
                    .append(location.trim()).append("%'");
            queryWrapper.inSql("camera_id", cameraSubQuery.toString());
        }

        // 分页查询数据
        Page<RealTimeDetectionRecord> page = realTimeService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 手动调用 count 查询获取符合条件的记录总数，并计算总页数（公式： (totalRecords + pageSize - 1) / pageSize ）
        long totalRecords = realTimeService.count(queryWrapper);
        int totalPages = (int) ((totalRecords + pageSize - 1) / pageSize);

        // 设置总记录数，不同版本的 MyBatis-Plus 会自动计算 pages 字段，也可以通过 page.getPages() 获取
        page.setTotal(totalRecords);

        // 如果前端需要自定义显示 totalPages，也可以通过返回 DTO 或者在响应头中传递
        // 此处仅为示例，用日志输出一下计算得到的总页数
        System.out.println("Total pages: " + totalPages);

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