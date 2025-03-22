package com.fwwb.vehicledetection.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class RecordCleanupScheduler {

    // 视频检测：输出目录
    private static final String OUTPUT_VIDEO_DIR = new File("./src/main/resources/yolo/image/videoOutput").getAbsolutePath();
    // 图像检测：输出目录
    private static final String OUTPUT_IMAGE_DIR = new File("./src/main/resources/yolo/image/imageOutput").getAbsolutePath();

    @Autowired
    private NonRealTimeDetectionRecordService recordService;

    // 每10min执行一次
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupExpiredRecords() {
        // 构建删除条件（PostgreSQL 语法）
        QueryWrapper<NonRealTimeDetectionRecord> wrapper = new QueryWrapper<>();
        wrapper.apply("time + (max_age || ' HOUR')::INTERVAL < NOW()");

        // 获取需要删除的记录
        List<NonRealTimeDetectionRecord> recordsToDelete = recordService.list(wrapper);

        // 删除记录并清理对应的文件夹
        for (NonRealTimeDetectionRecord record : recordsToDelete) {
            String expFolderName = record.getExp(); // 假设 exp 字段是文件夹名
            try {
                deleteFolder(OUTPUT_VIDEO_DIR, expFolderName);
                deleteFolder(OUTPUT_IMAGE_DIR, expFolderName);
            } catch (Exception e) {
                // 捕获异常并打印日志，然后继续执行
                System.err.println("删除文件夹时发生异常: " + e.getMessage());
                // 如果需要抛出异常后继续工作，可以选择不重新抛出异常
                // throw new RuntimeException("删除文件夹失败: " + expFolderName, e);
            }
        }

        // 执行删除并记录日志
        int deleted = recordService.getBaseMapper().delete(wrapper);
        System.out.println("已删除过期记录数: " + deleted);
    }

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