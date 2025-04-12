package com.fwwb.vehicledetection.controller.yolo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwwb.vehicledetection.domain.model.Camera;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.CameraService;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.VehicleService;
import com.fwwb.vehicledetection.util.WeatherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/yolo/realtime")
public class RealTimeYoloController {

    private static final String CONDA_PYTHON_PATH = "./src/main/resources/env/fwwb_yolo/python.exe";
    private static final String YOLO_SCRIPT_PATH = "main.py";
    private static final String YOLO_MODEL_PATH = "best.pt";
    private static final String REALTIME_OUTPUT_DIR = new File("./src/main/resources/yolo/realtime").getAbsolutePath();
    private static final String OUTPUT_SUFFIX = "_1"; // 明确指定输出目录后缀

    @Autowired
    private RealTimeDetectionRecordService realTimeDetectionRecordService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private CameraService cameraService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, RealtimeDetectionSession> activeSessions = new ConcurrentHashMap<>();

    @GetMapping("/stop/{cameraId}")
    public ResponseEntity<String> stopRealtimeDetection(@PathVariable Long cameraId) {
        RealtimeDetectionSession session = activeSessions.get(cameraId);
        if (session != null) {
            session.stop();
            activeSessions.remove(cameraId);
            return ResponseEntity.ok("Real-time detection stopped for cameraId: " + cameraId);
        }
        return ResponseEntity.badRequest().body("No active session found for cameraId: " + cameraId);
    }

    @GetMapping("/{cameraId}")
    public void realtimeDetection(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        // 摄像头验证逻辑
        Camera camera = validateCamera(cameraId, response);
        if (camera == null) return;

        // 转换设备ID为int
        int deviceIndex;
        try {
            deviceIndex = Integer.parseInt(String.valueOf(camera.getDeviceId()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的设备ID");
            return;
        }

        // 创建检测会话
        RealtimeDetectionSession session = new RealtimeDetectionSession(cameraId, deviceIndex);
        if (!session.start()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "无法启动实时检测");
            return;
        }
        activeSessions.put(cameraId, session);

        // 设置MJPEG流响应
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        ServletOutputStream outputStream = response.getOutputStream();

        try {
            while (session.isRunning()) {
                Path outputExpDir = waitForOutputDir(session);
                if (outputExpDir == null) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }

                // 处理最新帧和检测结果
                processFrameAndDetections(outputExpDir, outputStream, cameraId);
                TimeUnit.MILLISECONDS.sleep(125); // 控制帧率
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.stop();
            activeSessions.remove(cameraId);
        }
    }

    // 辅助方法 --------------------------------------------------

    private Camera validateCamera(Long cameraId, HttpServletResponse response) throws IOException {
        Camera camera = cameraService.getById(cameraId);
        if (camera == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "摄像头不存在");
            return null;
        }
        if (!"正常".equalsIgnoreCase(camera.getStatus())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "摄像头未启动");
            return null;
        }
        return camera;
    }

    private Path waitForOutputDir(RealtimeDetectionSession session) throws InterruptedException {
        // 最多等待3秒
        for (int i = 0; i < 30; i++) {
            Path dir = session.getActualOutputDir();
            if (dir != null && Files.exists(dir)) {
                return dir;
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
        return null;
    }

    private void processFrameAndDetections(Path outputExpDir, ServletOutputStream outputStream, Long cameraId) throws IOException {
        // 处理最新帧
        Optional<Path> latestFrame = findLatestFile(outputExpDir, ".jpg");
        if (latestFrame.isPresent()) {
            byte[] imageBytes = Files.readAllBytes(latestFrame.get());
            sendMjpegFrame(outputStream, imageBytes);
        }

        // 处理检测结果
        Optional<Path> latestJson = findLatestFile(outputExpDir, ".json");
        if (latestJson.isPresent()) {
            processJsonFile(latestJson.get(), outputExpDir.getFileName().toString(), cameraId);
        }
    }

    private Optional<Path> findLatestFile(Path dir, String extension) throws IOException {
        return Files.list(dir)
                .filter(p -> p.toString().endsWith(extension))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
    }

    private void sendMjpegFrame(ServletOutputStream outputStream, byte[] imageBytes) throws IOException {
        outputStream.write(("--frame\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
        outputStream.write(imageBytes);
        outputStream.write("\r\n".getBytes());
        outputStream.flush();
    }

    private void processJsonFile(Path jsonFile, String expFolderName, Long cameraId) {
        try {
            // 读取原始内容
            String jsonContent = Files.readString(jsonFile, StandardCharsets.UTF_8).trim();

            // 修复常见JSON格式问题
            jsonContent = fixJsonFormat(jsonContent);

            // 验证JSON有效性
            if (!isValidJson(jsonContent)) {
                System.err.println("修复后仍无效的JSON: " + jsonFile);
                backupCorruptedFile(jsonFile, jsonContent);
                return;
            }

            // 解析JSON
            List<Map<String, Object>> records = mapper.readValue(
                    jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 处理记录
            records.forEach(record -> processDetectionRecord(record, expFolderName, cameraId));

        } catch (Exception e) {
            System.err.println("处理JSON文件失败: " + jsonFile);
            e.printStackTrace();
        }
    }

    private String fixJsonFormat(String jsonContent) {
        // 1. 移除BOM头
        jsonContent = jsonContent.replaceAll("^\uFEFF", "");

        // 2. 修复尾部逗号问题（多层处理）
        jsonContent = jsonContent
                // 移除数组内的尾部逗号
                .replaceAll(",(\\s*)]", "$1]")
                // 移除对象内的尾部逗号
                .replaceAll(",(\\s*)}", "$1}");

        // 3. 确保是有效的数组结构
        if (!jsonContent.startsWith("[")) {
            jsonContent = jsonContent.startsWith("{")
                    ? "[" + jsonContent + "]"
                    : "[]";
        }

        // 4. 处理可能的中途截断
        int lastBrace = jsonContent.lastIndexOf('}');
        if (lastBrace == -1) {
            return "[]"; // 无有效内容返回空数组
        }
        jsonContent = jsonContent.substring(0, lastBrace + 1) + "]";

        // 5. 二次验证修复
        return jsonContent.replaceAll("(?m),\\s*\\]", "]");
    }

    private boolean isValidJson(String jsonContent) {
        try {
            mapper.readTree(jsonContent);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private void backupCorruptedFile(Path originalFile, String content) {
        try {
            Path backupDir = Paths.get(REALTIME_OUTPUT_DIR, "corrupted");
            Files.createDirectories(backupDir);

            Path backupFile = backupDir.resolve(
                    originalFile.getFileName().toString() + ".corrupted"
            );

            Files.writeString(backupFile, content, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("无法备份损坏的文件: " + originalFile);
            e.printStackTrace();
        }
    }

    private void processDetectionRecord(Map<String, Object> record, String expFolderName, Long cameraId) {
        try {
            String type = (String) record.get("type");
            String licensePlate = (String) record.get("license_plate"); // 可以为 null
            Double confidence = Double.valueOf(record.get("confidence").toString());

            // 验证输入
            if (type == null || type.trim().isEmpty()) {
                System.err.println("检测记录缺少有效的车辆类型: " + record);
                return;
            }
            if (cameraId < 1 || cameraId > Integer.MAX_VALUE) {
                System.err.println("无效的 cameraId: " + cameraId + ", 记录: " + record);
                return;
            }

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
                    // 当 license_plate 为 null 时，仅按 type 查询
                    vehicle = vehicleService.lambdaQuery()
                            .eq(Vehicle::getType, type)
                            .one();
                }

                // 如果未找到车辆，创建新车辆
                if (vehicle == null) {
                    vehicle = new Vehicle();
                    vehicle.setType(type);
                    vehicle.setLicence(licensePlate); // 可以为 null

                    // 生成 vehicle_id
                    long vehicleId;
                    if (licensePlate == null || licensePlate.trim().isEmpty()) {
                        // 当 license_plate 为 null，使用 type 的哈希值
                        vehicleId = Math.abs(type.hashCode() % Integer.MAX_VALUE) + 1; // 确保在 1 到 Integer.MAX_VALUE
                    } else {
                        // 当 license_plate 非空，使用 type + license_plate 的哈希值
                        String combined = type + licensePlate;
                        vehicleId = Math.abs(combined.hashCode() % Integer.MAX_VALUE) + 1; // 确保在 1 到 Integer.MAX_VALUE
                    }

                    // 设置 vehicleId（模仿非实时代码逻辑）
                    vehicle.setVehicleId(vehicleId);

                    // 验证 vehicleId 是否在 INTEGER 范围内
                    if (vehicleId < 1 || vehicleId > Integer.MAX_VALUE) {
                        System.err.println("生成的 vehicleId 超出 INTEGER 范围: " + vehicleId + ", 记录: " + record);
                        return;
                    }

                    try {
                        vehicleService.save(vehicle);
                    } catch (org.springframework.dao.DuplicateKeyException e) {
                        // 重试查询
                        if (licensePlate != null && !licensePlate.trim().isEmpty()) {
                            vehicle = vehicleService.lambdaQuery()
                                    .eq(Vehicle::getType, type)
                                    .eq(Vehicle::getLicence, licensePlate)
                                    .one();
                        } else {
                            vehicle = vehicleService.lambdaQuery()
                                    .eq(Vehicle::getType, type)
                                    .one();
                        }
                        if (vehicle == null) {
                            System.err.println("无法为类型和车牌创建或找到车辆: type=" + type + ", licensePlate=" + licensePlate + ", 记录: " + record);
                            return;
                        }
                    }
                }
            }

            // 验证 vehicle_id
            Long vehicleId = vehicle.getVehicleId();
            if (vehicleId == null || vehicleId < 1 || vehicleId > Integer.MAX_VALUE) {
                System.err.println("无效的 vehicleId: " + vehicleId + "，车辆类型: " + type + ", 记录: " + record);
                return;
            }

            // 处理天气数据
            Map<String, Object> weatherData = WeatherUtil.getCurrentWeather();
            double temperature = 25;
            String weather = String.valueOf(weatherData.get("weather"));

            // 保存检测记录
            RealTimeDetectionRecord rtRecord = new RealTimeDetectionRecord();
            rtRecord.setCameraId(cameraId);
            rtRecord.setTime(LocalDateTime.now());
            rtRecord.setConfidence(confidence);
            rtRecord.setTemperature(temperature);
            rtRecord.setWeather(weather);
            rtRecord.setVehicleId(vehicleId);
            rtRecord.setVehicleStatus(null);
            rtRecord.setMaxAge(168L);
            rtRecord.setExp(expFolderName);

            System.out.println("正在保存 RealTimeDetectionRecord: cameraId=" + cameraId +
                    ", vehicleId=" + vehicleId + ", maxAge=" + rtRecord.getMaxAge());

            realTimeDetectionRecordService.save(rtRecord);
        } catch (Exception e) {
            System.err.println("处理单条检测记录失败: " + record + ", 错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 实时检测会话内部类 --------------------------------------------------
    private class RealtimeDetectionSession {
        private final Long cameraId;
        private final int deviceIndex;
        private Process process;
        private volatile boolean running = false;
        private Path outputParentDir;

        public RealtimeDetectionSession(Long cameraId, int deviceIndex) {
            this.cameraId = cameraId;
            this.deviceIndex = deviceIndex;
        }

        public boolean start() {
            try {
                // 创建父目录（不带后缀）
                String parentFolderName = "realtimeExp_" + System.currentTimeMillis();
                outputParentDir = Paths.get(REALTIME_OUTPUT_DIR, parentFolderName);
                Files.createDirectories(outputParentDir);

                // 启动Python进程
                ProcessBuilder pb = new ProcessBuilder(
                        CONDA_PYTHON_PATH,
                        YOLO_SCRIPT_PATH,
                        "--model", YOLO_MODEL_PATH,
                        "--source", String.valueOf(deviceIndex),
                        "--project", REALTIME_OUTPUT_DIR,
                        "--name", parentFolderName,
                        "--json",
                        "--show"
                );
                pb.directory(new File("./src/main/resources/yolo"));
                pb.redirectErrorStream(true);
                pb.environment().put("OPENCV_VIDEOIO_PRIORITY_DSHOW", "980");

                process = pb.start();
                logProcessOutput(process);
                running = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void logProcessOutput(Process process) {
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[YOLO] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void stop() {
            running = false;
            try {
                // 创建停止信号文件
                Files.createFile(outputParentDir.resolve("stop.txt"));
                // 等待进程退出
                if (process != null && process.isAlive()) {
                    process.destroy();
                    process.waitFor(3, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isRunning() {
            return running;
        }

        public Path getActualOutputDir() {
            if (outputParentDir != null) {
                Path actualDir = outputParentDir.resolveSibling(outputParentDir.getFileName() + OUTPUT_SUFFIX);
                return Files.exists(actualDir) ? actualDir : null;
            }
            return null;
        }
    }
}