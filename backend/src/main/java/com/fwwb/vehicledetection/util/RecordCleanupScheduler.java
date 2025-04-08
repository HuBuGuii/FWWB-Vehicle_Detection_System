package com.fwwb.vehicledetection.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Transactional
@Component
public class RecordCleanupScheduler {

    // 非实时检测输出目录
    private static final String OUTPUT_VIDEO_DIR = new File("./src/main/resources/yolo/video/videoOutput").getAbsolutePath();
    private static final String OUTPUT_IMAGE_DIR = new File("./src/main/resources/yolo/image/imageOutput").getAbsolutePath();
    // 实时检测输出目录（用于存放实时检测生成的 exp 文件夹）
    private static final String REALTIME_OUTPUT_DIR = new File("./src/main/resources/yolo/realtime").getAbsolutePath();

    @Autowired
    private NonRealTimeDetectionRecordService recordService;

    @Autowired
    private RealTimeDetectionRecordService realTimeDetectionRecordService;

    /**
     * 每10分钟执行一次非实时记录的清理任务（原有逻辑，不做任何修改）
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupExpiredRecords() {
        // 构建删除条件（PostgreSQL语法）
        QueryWrapper<NonRealTimeDetectionRecord> wrapper = new QueryWrapper<>();
        wrapper.apply("time + (max_age || ' HOUR')::INTERVAL < NOW()");

        // 获取需要删除的记录
        List<NonRealTimeDetectionRecord> recordsToDelete = recordService.list(wrapper);

        // 删除记录并清理对应的文件夹
        for (NonRealTimeDetectionRecord record : recordsToDelete) {
            String expFolderName = record.getExp(); // 假设 exp 字段存放的是输出文件夹名称
            try {
                deleteFolder(OUTPUT_VIDEO_DIR, expFolderName);
                deleteFolder(OUTPUT_IMAGE_DIR, expFolderName);
            } catch (Exception e) {
                // 捕获异常并打印日志，然后继续执行
                System.err.println("删除非实时记录对应文件夹时发生异常: " + e.getMessage());
            }
        }

        // 执行数据库删除并打印日志
        int deleted = recordService.getBaseMapper().delete(wrapper);
        System.out.println("已删除过期非实时检测记录数: " + deleted);
    }

    /**
     * 新增：每10分钟执行一次实时检测记录的清理任务
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupExpiredRealTimeRecords() {
        // 构建删除条件，使用与非实时记录相同的判断方式
        QueryWrapper<RealTimeDetectionRecord> wrapper = new QueryWrapper<>();
        wrapper.apply("time + (max_age || ' HOUR')::INTERVAL < NOW()");

        // 获取需要删除的实时记录
        List<RealTimeDetectionRecord> recordsToDelete = realTimeDetectionRecordService.list(wrapper);

        // 如果实时记录中保存了输出文件夹名称信息（例如 exp 字段），则执行文件夹删除
        for (RealTimeDetectionRecord record : recordsToDelete) {
            String expFolderName = record.getExp();  // 假设实时记录中也存储检测结果对应的 exp 文件夹名称（若没有，可以忽略此逻辑）
            if(expFolderName != null && !expFolderName.isEmpty()){
                try {
                    deleteFolder(REALTIME_OUTPUT_DIR, expFolderName);
                } catch (Exception e) {
                    System.err.println("删除实时记录对应文件夹时发生异常: " + e.getMessage());
                }
            }
        }

        // 删除数据库中过期的实时检测记录
        int deleted = realTimeDetectionRecordService.getBaseMapper().delete(wrapper);
        System.out.println("已删除过期实时检测记录数: " + deleted);
    }

    /**
     * 删除指定目录下的文件夹
     *
     * @param baseDir    基础目录路径
     * @param folderName 文件夹名称
     * @throws Exception 如果删除失败则抛出异常
     */
    private void deleteFolder(String baseDir, String folderName) throws Exception {
        File folder = new File(baseDir, folderName);
        if (folder.exists() && folder.isDirectory()) {
            try {
                deleteDirectory(folder);
                System.out.println("已删除文件夹: " + folder.getAbsolutePath());
            } catch (Exception e) {
                throw new Exception("无法删除文件夹: " + folder.getAbsolutePath(), e);
            }
        } else {
            throw new Exception("文件夹不存在或不是目录: " + folder.getAbsolutePath());
        }
    }

    /**
     * 递归删除目录中的所有文件和子目录
     *
     * @param directory 目标目录
     * @throws Exception 如果删除任意文件或目录失败则抛出异常
     */
    private void deleteDirectory(File directory) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new Exception("无法删除文件: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new Exception("无法删除目录: " + directory.getAbsolutePath());
        }
    }
}