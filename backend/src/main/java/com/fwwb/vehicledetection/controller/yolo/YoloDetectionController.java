package com.fwwb.vehicledetection.controller.yolo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.VehicleService;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;


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
    private static final String INPUT_FRAMES_DIR = new File("src\\main\\resources\\yolo\\video\\videoFrame").getAbsolutePath();

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
     * 优化后的视频检测控制器API
     * POST /api/yolo/video
     * 上传视频文件，先使用 JavaCV 抽帧到 8fps，生成图像文件到抽帧目录，
     * 然后调用 Python 脚本检测，将输出的结果写入数据库。
     */
    @PostMapping("/video")
    public ResponseEntity<String> detectVideo(@RequestParam("files") MultipartFile[] files) {
        try {
            // 创建输入、抽帧和输出目录（如果不存在）
            Files.createDirectories(Paths.get(INPUT_VIDEO_DIR));
            Files.createDirectories(Paths.get(INPUT_FRAMES_DIR));
            Files.createDirectories(Paths.get(OUTPUT_VIDEO_DIR));

            // 保存上传的视频文件到输入目录，并为每个视频抽帧
            for (MultipartFile file : files) {
                String uniqueID = UUID.randomUUID().toString();
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".mp4";
                String inputFilename = "video_" + uniqueID + extension;
                Path inputVideoPath = Paths.get(INPUT_VIDEO_DIR, inputFilename);

                // 保存视频文件
                Files.write(inputVideoPath, file.getBytes());

                // 抽帧到 8fps，保存到抽帧目录
                extractFramesAt8fps(inputVideoPath, Paths.get(INPUT_FRAMES_DIR));
            }

            // 构建并执行 Python 脚本命令，更新 --source 为抽帧目录
            ProcessBuilder pb = new ProcessBuilder(
                    CONDA_PYTHON_PATH,
                    YOLO_SCRIPT_PATH,
                    "--model", YOLO_MODEL_PATH,
                    "--source", INPUT_FRAMES_DIR, // 改为抽帧目录
                    "--project", OUTPUT_VIDEO_DIR,
                    "--name", "videoExp",
                    "--json",
                    "--mode","image"
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

            // 删除输入视频目录和抽帧目录中的文件
            Files.list(Paths.get(INPUT_VIDEO_DIR)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Files.list(Paths.get(INPUT_FRAMES_DIR)).forEach(path -> {
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
                        vehicle.setLicence(licensePlate); // 可能为 null
                        try {
                            vehicleService.save(vehicle);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("视频处理失败: " + e.getMessage());
        }
    }

    /**
     * 新增方法：抽帧视频到 8fps，保存为 JPG 文件
     * @param videoPath 视频文件路径
     * @param outputDir 抽帧输出目录
     * @throws Exception 抽帧过程中的异常
     */
    private void extractFramesAt8fps(Path videoPath, Path outputDir) throws Exception {
        // 确保输出目录存在
        Files.createDirectories(outputDir);

        // 使用 FFmpegFrameGrabber 打开视频
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath.toString());
        grabber.start();

        // 计算目标帧间隔（1/8 秒，单位为秒）
        double targetInterval = 1.0 / 8.0; // 8fps
        double lastTime = -targetInterval; // 初始化为负值以确保第一帧被提取
        int frameCount = 0;

        // 逐帧处理
        Java2DFrameConverter converter = new Java2DFrameConverter();
        while (true) {
            Frame frame = grabber.grabImage(); // 仅抓取图像帧
            if (frame == null) break;

            // 获取当前帧时间（秒）
            double currentTime = grabber.getTimestamp() / 1_000_000.0; // 微秒转为秒

            // 如果时间间隔达到 1/8 秒，保存该帧
            if (currentTime - lastTime >= targetInterval) {
                BufferedImage image = converter.getBufferedImage(frame);
                String frameFilename = String.format("frame_%06d.jpg", frameCount);
                Path framePath = outputDir.resolve(frameFilename);
                ImageIO.write(image, "jpg", framePath.toFile());
                lastTime = currentTime;
                frameCount++;
            }
        }

        // 释放资源
        grabber.stop();
        grabber.close();
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

                // 规范化 licence：空字符串或 null 都视为 null
                if (licence != null && licence.trim().isEmpty()) {
                    licence = null;
                }

                long vehicleId = getVehicleIdByType(type, licence);

                // 查找或创建车辆
                Vehicle vehicle = null;
                synchronized (this) { // 防止竞争条件
                    // 优先按 type 和 licence 查询（如果 licence 非 null）
                    if (licence != null) {
                        vehicle = vehicleService.lambdaQuery()
                                .eq(Vehicle::getType, type)
                                .eq(Vehicle::getLicence, licence)
                                .one();
                    } else {
                        // 如果 licence 为 null，按 vehicleId 查询
                        vehicle = vehicleService.getById(vehicleId);
                    }

                    // 如果未找到车辆，则创建新记录
                    if (vehicle == null) {
                        vehicle = new Vehicle();
                        vehicle.setVehicleId(vehicleId);
                        vehicle.setType(type);
                        vehicle.setLicence(licence); // 可能为 null
                        try {
                            vehicleService.save(vehicle);
                        } catch (Exception e) {
                            // 捕获数据库唯一约束异常
                            if (e.getMessage().contains("vehicle_licence_key")) {
                                System.out.println("检测到重复的车牌号（licence=" + licence + "），跳过插入新车辆记录。");
                                // 再次尝试查找现有记录
                                vehicle = vehicleService.lambdaQuery()
                                        .eq(Vehicle::getType, type)
                                        .eq(Vehicle::getLicence, licence)
                                        .one();
                                if (vehicle == null) {
                                    System.out.println("无法找到匹配的车辆记录，继续处理其他记录。");
                                    continue; // 跳过当前记录的后续处理，直接进入下一个循环
                                }
                            } else {
                                throw e; // 其他异常继续抛出
                            }
                        }
                    }
                }

                // 保存检测记录
                NonRealTimeDetectionRecord record = new NonRealTimeDetectionRecord();
                record.setUserId(3L);
                record.setTime(LocalDateTime.now());
                record.setConfidence(confidence);
                record.setVehicleId(vehicle.getVehicleId()); // 使用实际的 vehicleId
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