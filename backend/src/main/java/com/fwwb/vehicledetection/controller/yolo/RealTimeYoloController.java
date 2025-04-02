package com.fwwb.vehicledetection.controller.yolo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwwb.vehicledetection.domain.model.RealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.CameraService;
import com.fwwb.vehicledetection.service.RealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.VehicleService;
import com.fwwb.vehicledetection.util.WeatherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/yolo/realtime")
public class RealTimeYoloController {

    // Python环境和脚本相关参数
    private static final String CONDA_PYTHON_PATH = "./src/main/resources/env/fwwb_yolo/python.exe";
    private static final String YOLO_SCRIPT_PATH = "main.py";
    private static final String YOLO_MODEL_PATH = "best.pt";
    // 实时检测的输出目录（所有实时检测 exp 文件夹均位于此目录下）
    private static final String REALTIME_OUTPUT_DIR = new File("./src/main/resources/yolo/realtime").getAbsolutePath();

    @Autowired
    private RealTimeDetectionRecordService realTimeDetectionRecordService;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private CameraService cameraService;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 统一实时检测接口：
     * 接口 URL: /api/yolo/realtime/{cameraId}
     *
     * 1. 判断摄像头是否存在且状态正常；
     * 2. 创建实时检测会话（启动 Python 进程，采集并检测，输出实时 jpg 和 JSON 结果文件）；
     * 3. 以 MJPEG 流形式返回最新 jpg 帧，并周期性读取 JSON 检测结果写入数据库，
     *    写入表：realTimeDetectionRecord（字段参见表结构）。
     * 4. 客户端断开连接时，在 finally 块创建停止信号文件，通知 Python 端退出采集循环。
     */
    @GetMapping("/{cameraId}")
    public void realtimeDetection(@PathVariable Long cameraId, HttpServletResponse response) throws IOException {
        // 检查摄像头是否存在及状态是否"正常"
        var camera = cameraService.getById(cameraId);
        if (camera == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "摄像头不存在");
            return;
        }
        if (!"正常".equalsIgnoreCase(camera.getStatus())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "摄像头未启动");
            return;
        }

        int deviceIndex;
        try {
            deviceIndex = Integer.parseInt(String.valueOf(camera.getDeviceId()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的设备ID");
            return;
        }

        // 创建新的实时检测会话（启动 Python 进程）
        RealtimeDetectionSession session = new RealtimeDetectionSession(cameraId, deviceIndex);
        if (!session.start()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "无法启动实时检测");
            return;
        }

        // 设置响应内容类型为 MJPEG 流
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        ServletOutputStream outputStream = response.getOutputStream();

        try {
            while (session.isRunning()) {
                Path outputExpDir = session.getOutputExpDir();
                if (outputExpDir == null) {
                    TimeUnit.MILLISECONDS.sleep(125);
                    continue;
                }
                String expFolderName = outputExpDir.getFileName().toString();

                // 查找最新的 jpg 帧——Python 每次更新 latest.jpg
                Optional<Path> latestFrameOpt = Files.list(outputExpDir)
                        .filter(p -> p.toString().endsWith(".jpg"))
                        .max(Comparator.comparing(p -> {
                            try {
                                return Files.getLastModifiedTime(p);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }));
                if (latestFrameOpt.isEmpty()) {
                    TimeUnit.MILLISECONDS.sleep(42);
                    continue;
                }
                Path latestFrameFile = latestFrameOpt.get();
                byte[] imageBytes = Files.readAllBytes(latestFrameFile);

                // 查找检测结果 JSON 文件
                // 修改过滤条件：既匹配 detections.json 也匹配 detections_ 开头的文件
                Optional<Path> jsonFileOpt = Files.list(outputExpDir)
                        .filter(p -> {
                            String name = p.getFileName().toString();
                            return (name.startsWith("detections") && name.endsWith(".json"));
                        })
                        .findFirst();
                if (jsonFileOpt.isPresent()) {
                    Path jsonFile = jsonFileOpt.get();
                    String jsonContent = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8).trim();
                    // 去除尾部可能多余的逗号
                    if (jsonContent.endsWith(",")) {
                        jsonContent = jsonContent.substring(0, jsonContent.length() - 1).trim();
                    }
                    // 如果不是 JSON 数组则包裹成数组
                    if (!jsonContent.startsWith("[")) {
                        jsonContent = "[" + jsonContent + "]";
                    }
                    List<Map<String, Object>> records = mapper.readValue(jsonContent,
                            new TypeReference<List<Map<String, Object>>>() {});
                    for (Map<String, Object> recordMap : records) {
                        String type = (String) recordMap.get("type");
                        Double confidence = Double.valueOf(recordMap.get("confidence").toString());
                        long vehicleId = getVehicleIdByType(type);
                        // 检查或创建车辆记录
                        Vehicle vehicle = vehicleService.getById(vehicleId);
                        if (vehicle == null) {
                            vehicle = new Vehicle();
                            vehicle.setVehicleId(vehicleId);
                            vehicle.setType(type);
                            vehicle.setLicence(null);
                            vehicleService.save(vehicle);
                        }
                        // 获取天气数据（例如返回 { "temperature": "25°C", "weather": "晴" }）
                        Map<String, Object> weatherData = WeatherUtil.getCurrentWeather();
                        // 原始温度字符串
                        String tempStr = String.valueOf(weatherData.get("temperature"));
                        // 通过正则表达式去除非数字和小数点的字符
                        tempStr = tempStr.replaceAll("[^0-9.]+", "");
                        double temperature = Double.valueOf(tempStr);
                        String weather = String.valueOf(weatherData.get("weather"));

                        // 创建并保存实时检测记录到 realTimeDetectionRecord 表
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
                        realTimeDetectionRecordService.save(rtRecord);
                    }
                }

                // 将最新 jpg 帧数据写入 MJPEG 流
                outputStream.write(("--frame\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
                outputStream.write(imageBytes);
                outputStream.write("\r\n".getBytes());
                outputStream.flush();

                // 控制帧率 8 帧/s
                TimeUnit.MILLISECONDS.sleep(125);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 客户端断开或出现异常时，通知 Python 脚本退出
            session.stop();
        }
    }

    /**
     * 简单映射：取 type 的hashCode 绝对值作为 vehicleId
     */
    private long getVehicleIdByType(String type) {
        return Math.abs(type.hashCode());
    }

    /**
     * 内部类：管理实时检测会话，
     * 包括启动 Python 检测进程、传递参数、创建输出目录以及停止会话时写入停止信号文件
     */
    private class RealtimeDetectionSession {
        private Long cameraId;
        private int deviceIndex;
        private Process process;
        private volatile boolean running = false;
        // 本次检测生成的 exp 输出文件夹路径
        private Path outputExpDir;

        public RealtimeDetectionSession(Long cameraId, int deviceIndex) {
            this.cameraId = cameraId;
            this.deviceIndex = deviceIndex;
        }

        public boolean start() {
            try {
                Files.createDirectories(Paths.get(REALTIME_OUTPUT_DIR));
                // 按照"realtimeExp_" + 时间戳生成输出目录
                String expFolderName = "realtimeExp_" + System.currentTimeMillis();
                outputExpDir = Paths.get(REALTIME_OUTPUT_DIR, expFolderName);
                Files.createDirectories(outputExpDir);

                // 构造 Python 检测脚本命令
                List<String> command = Arrays.asList(
                        CONDA_PYTHON_PATH,
                        YOLO_SCRIPT_PATH,
                        "--model", YOLO_MODEL_PATH,
                        "--source", String.valueOf(deviceIndex),
                        "--project", REALTIME_OUTPUT_DIR,
                        "--name", expFolderName,
                        "--txt",
                        "--json",
                        "--save-video",
                        "--show"
                );
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File("./src/main/resources/yolo"));
                pb.redirectErrorStream(true);
                process = pb.start();

                // 后台线程读取并打印 Python 脚本输出日志
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[RealTime Detection] " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                running = true;
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public void stop() {
            running = false;
            // 写入停止信号文件 stop.txt，通知 Python 端退出采集循环
            if (outputExpDir != null) {
                File stopFile = new File(outputExpDir.toFile(), "stop.txt");
                try {
                    if (!stopFile.exists()) {
                        stopFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 等待 Python 进程退出5秒，超时则强制销毁
            if (process != null) {
                try {
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isRunning() {
            return running;
        }

        public Path getOutputExpDir() {
            return outputExpDir;
        }
    }
}