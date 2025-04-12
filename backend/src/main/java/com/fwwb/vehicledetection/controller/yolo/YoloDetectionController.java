package com.fwwb.vehicledetection.controller.yolo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;


@RestController
@RequestMapping("/api/yolo")
public class YoloDetectionController {
    // 修改后的 Python 脚本路径（位于 yolo 文件夹下的 main.py）
    private static final String YOLO_SCRIPT_PATH = "main.py";
    // 使用新模型（例如 best.pt）
    private static final String YOLO_MODEL_PATH = "best.pt";
    // 修改为新虚拟环境的 Python 路径（fwwb_yolo）
    private static final String CONDA_PYTHON_PATH = "src\\main\\resources\\env\\fwwb_yolo\\python.exe";

    // 视频检测：输入/输出目录
    private static final String INPUT_VIDEO_DIR = new File("src\\main\\resources\\yolo\\video\\videoInput").getAbsolutePath();
    private static final String OUTPUT_VIDEO_DIR = new File("src\\main\\resources\\yolo\\video\\videoOutput").getAbsolutePath();

    // 图像检测：输入/输出目录
    private static final String INPUT_IMAGE_DIR = new File("src\\main\\resources\\yolo\\image\\imageInput").getAbsolutePath();
    private static final String OUTPUT_IMAGE_DIR = new File("src\\main\\resources\\yolo\\image\\imageOutput").getAbsolutePath();

    @Autowired
    private NonRealTimeDetectionRecordService nonRealTimeService;
    @Autowired
    private VehicleService vehicleService;

    /**
     * 根据检测结果中的 type 字段生成 vehicleId（这里采用 type 的 hash 值作为简单映射方式）
     */
    private long getVehicleIdByType(String type, String licensePlate) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        // 生成 vehicle_id
        long vehicleId;
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            // 当 license_plate 为 null 或空字符串，使用 type 的哈希值
            vehicleId = Math.abs(type.hashCode() % Integer.MAX_VALUE) + 1; // 确保在 1 到 Integer.MAX_VALUE
        } else {
            // 当 license_plate 非空，使用 type + license_plate 的哈希值
            String combined = type + licensePlate;
            vehicleId = Math.abs(combined.hashCode() % Integer.MAX_VALUE) + 1; // 确保在 1 到 Integer.MAX_VALUE
        }

        return vehicleId;
    }

    /**
     * POST /api/yolo/video
     * 上传视频文件，经 Python 脚本检测后返回处理过的视频 exp 文件夹名称，
     * 同时解析结果 JSON 文件，将检测记录更新到数据库中。
     */
    @PostMapping("/video")
    public ResponseEntity<String> detectVideo(@RequestParam("files") MultipartFile[] files) {
        try {
            // 创建输入输出目录（如果不存在）
            Files.createDirectories(Paths.get(INPUT_VIDEO_DIR));
            Files.createDirectories(Paths.get(OUTPUT_VIDEO_DIR));

            // 保存上传的视频文件到输入目录中
            for (MultipartFile file : files) {
                String uniqueID = UUID.randomUUID().toString();
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".mp4";
                String inputFilename = "video_" + uniqueID + extension;
                Path inputVideoPath = Paths.get(INPUT_VIDEO_DIR, inputFilename);
                Files.write(inputVideoPath, file.getBytes());
            }

            // 构建并执行 Python 脚本命令，更新命令参数，启用 --json 与 --save-video 参数
            ProcessBuilder pb = new ProcessBuilder(
                    CONDA_PYTHON_PATH,
                    YOLO_SCRIPT_PATH,
                    "--model", YOLO_MODEL_PATH,
                    "--source", INPUT_VIDEO_DIR,
                    "--project", OUTPUT_VIDEO_DIR,
                    "--name", "videoExp",
                    "--save-video",
                    "--json"
            );
            // 设置工作目录为 main.py 所在目录
            pb.directory(new File("./src/main/resources/yolo"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 输出 Python 脚本日志
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Video Detection] " + line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("YOLO视频检测失败，退出码：" + exitCode);
            }

            // 删除输入目录中的视频文件
            Files.list(Paths.get(INPUT_VIDEO_DIR)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 获取最新的输出 exp 文件夹（名称以 videoExp 开头）
            Path outputExpDir = Files.list(Paths.get(OUTPUT_VIDEO_DIR))
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("videoExp"))
                    .max(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                    .orElseThrow(() -> new RuntimeException("未找到视频输出目录"));

            // 在输出 exp 文件夹中查找 JSON 检测结果文件（文件名前缀 detections_）
            Path jsonResultFile = Files.list(outputExpDir)
                    .filter(path -> path.getFileName().toString().startsWith("detections_")
                            && path.toString().endsWith(".json"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到视频检测结果 JSON 文件"));

            // 读取整个 JSON 文件，并对格式进行转换
            String jsonContent = new String(Files.readAllBytes(jsonResultFile), StandardCharsets.UTF_8).trim();
            // 如果文件结束处有多余的逗号，则删除
            if (jsonContent.endsWith(",")) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1).trim();
            }
            // 如果文件不是以 [ 开始，则说明不是一个完整的 JSON 数组，需要手动包裹起来
            if (!jsonContent.startsWith("[")) {
                jsonContent = "[" + jsonContent + "]";
            }

            // 使用 Jackson 将修改后的 JSON 字符串解析为 List<Map<String, Object>>
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> records = mapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> recordMap : records) {
                String type = (String) recordMap.get("type");
                Double confidence = Double.valueOf(recordMap.get("confidence").toString());
                String licensePlate = (String) recordMap.get("license_plate"); // 假设车牌字段为 license_plate
                long vehicleId = getVehicleIdByType(type, licensePlate); // 使用扩展后的方法

                // 查找或创建车辆
                Vehicle vehicle = null;
                synchronized (this) { // 防止竞争条件
                    // 首先尝试按 type 和 license_plate 查询
                    if (licensePlate != null && !licensePlate.trim().isEmpty()) {
                        vehicle = vehicleService.lambdaQuery()
                                .eq(Vehicle::getType, type)
                                .eq(Vehicle::getLicence, licensePlate)
                                .one();
                    } else {
                        // 当 license_plate 为 null 或空时，按 vehicleId 查询
                        vehicle = vehicleService.getById(vehicleId);
                    }

                    // 如果数据库中不存在该车辆记录，则创建一条新记录
                    if (vehicle == null) {
                        vehicle = new Vehicle();
                        vehicle.setVehicleId(vehicleId);
                        vehicle.setType(type);
                        vehicle.setLicence(licensePlate != null && !licensePlate.trim().isEmpty() ? licensePlate : null);
                        vehicleService.save(vehicle);
                    }
                }
                // 创建并保存检测记录
                NonRealTimeDetectionRecord record = new NonRealTimeDetectionRecord();
                record.setUserId(3L);
                record.setTime(LocalDateTime.now());
                record.setConfidence(confidence);
                record.setVehicleId(vehicleId);
                record.setVehicleStatus("Nah");
                record.setMaxAge(168L);
                record.setExp(outputExpDir.getFileName().toString());
                nonRealTimeService.save(record);
            }

            return ResponseEntity.ok("视频检测完成，结果已保存至 " + outputExpDir.getFileName().toString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("视频处理失败: " + e.getMessage());
        }
    }

    /**
     * POST /api/yolo/image
     * 上传图片文件，经 Python 脚本检测后返回处理过的图片 exp 文件夹名称，
     * 同时解析结果 JSON 文件，将检测记录更新到数据库中。
     */
    @PostMapping("/image")
    public ResponseEntity<String> detectImage(@RequestParam("files") MultipartFile[] files) {
        try {
            // 创建输入输出目录（如果不存在）
            Files.createDirectories(Paths.get(INPUT_IMAGE_DIR));
            Files.createDirectories(Paths.get(OUTPUT_IMAGE_DIR));

            // 保存上传的图片到输入目录
            for (MultipartFile file : files) {
                String uniqueID = UUID.randomUUID().toString();
                Path inputImage = Paths.get(INPUT_IMAGE_DIR, "input_" + uniqueID + ".jpg");
                Files.write(inputImage, file.getBytes());
            }

            // 构造调用 Python 脚本命令，启用 --json 参数
            ProcessBuilder pb = new ProcessBuilder(
                    CONDA_PYTHON_PATH,
                    YOLO_SCRIPT_PATH,
                    "--model", YOLO_MODEL_PATH,
                    "--source", INPUT_IMAGE_DIR,
                    "--project", OUTPUT_IMAGE_DIR,
                    "--name", "imageExp",
                    "--json",
                    "--mode","image"
            );
            pb.directory(new File("./src/main/resources/yolo"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 输出 Python 脚本日志
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Image Detection] " + line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("YOLO图像检测脚本执行失败，退出码：" + exitCode);
            }

            // 删除输入目录中的图片
            Files.list(Paths.get(INPUT_IMAGE_DIR)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 获取最新的输出 exp 文件夹（名称以 imageExp 开头）
            Path outputExpDir = Files.list(Paths.get(OUTPUT_IMAGE_DIR))
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("imageExp"))
                    .max(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                    .orElseThrow(() -> new RuntimeException("未找到图像输出目录"));

            // 查找输出文件夹中的 JSON 结果文件
            Path jsonResultFile = Files.list(outputExpDir)
                    .filter(path -> path.getFileName().toString().startsWith("detections_")
                            && path.toString().endsWith(".json"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到图像检测结果 JSON 文件"));

            // 读取整个 JSON 文件，将其包装成一个数组再解析
            String jsonContent = new String(Files.readAllBytes(jsonResultFile), StandardCharsets.UTF_8).trim();
            if (jsonContent.endsWith(",")) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 1).trim();
            }
            if (!jsonContent.startsWith("[")) {
                jsonContent = "[" + jsonContent + "]";
            }

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> records = mapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>(){});

            for (Map<String, Object> recordMap : records) {
                String type = (String) recordMap.get("type");
                String licence = (String) recordMap.get("license_plate");
                Double confidence = Double.valueOf(recordMap.get("confidence").toString());
                long vehicleId = getVehicleIdByType(type, licence);

                Vehicle vehicle = vehicleService.getById(vehicleId);
                if (vehicle == null) {
                    vehicle = new Vehicle();
                    vehicle.setVehicleId(vehicleId);
                    vehicle.setType(type);
                    vehicle.setLicence(licence);
                    vehicleService.save(vehicle);
                }
                NonRealTimeDetectionRecord record = new NonRealTimeDetectionRecord();
                record.setUserId(3L);
                record.setTime(LocalDateTime.now());
                record.setConfidence(confidence);
                record.setVehicleId(vehicleId);
                record.setVehicleStatus("Nah");
                record.setMaxAge(168L);
                record.setExp(outputExpDir.getFileName().toString());
                nonRealTimeService.save(record);
            }

            return ResponseEntity.ok("图像检测完成，结果已保存至 " + outputExpDir.getFileName().toString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("内部错误: " + e.getMessage());
        }
    }
}