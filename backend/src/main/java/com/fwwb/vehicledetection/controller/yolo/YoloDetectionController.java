// File: src/main/java/com/fwwb/vehicledetection/controller/detection/YoloDetectionController.java
package com.fwwb.vehicledetection.controller.yolo;

import com.fwwb.vehicledetection.domain.model.NonRealTimeDetectionRecord;
import com.fwwb.vehicledetection.domain.model.Vehicle;
import com.fwwb.vehicledetection.service.NonRealTimeDetectionRecordService;
import com.fwwb.vehicledetection.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/yolo")
public class YoloDetectionController {
    private static final String YOLO_SCRIPT_PATH = "detect.py"; // 改为相对路径
    private static final String YOLO_MODEL_PATH = "yolov5s.pt"; // 改为相对路径
    private static final String CONDA_PYTHON_PATH = "D:/Work/Tools/Anaconda/envs/yolov5/python.exe";


    /**
     * POST /api/yolo/video
     * 上传视频文件，经 Python 脚本检测后返回处理过的视频
     */
    private static final String INPUT_VIDEO_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbVideo/input").getAbsolutePath();
    private static final String OUTPUT_VIDEO_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbVideo/output").getAbsolutePath();

    @PostMapping("/video")
    public ResponseEntity<String> detectVideo(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
        System.out.println("Content-Type: " + request.getContentType());

        try {
            // 创建输入输出目录（如果不存在）
            Files.createDirectories(Paths.get(INPUT_VIDEO_DIR));
            Files.createDirectories(Paths.get(OUTPUT_VIDEO_DIR));

            // 保存上传的视频文件
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

            // 构建并执行Python脚本命令（添加 --save-txt 参数）
            ProcessBuilder pb = new ProcessBuilder(
                    CONDA_PYTHON_PATH,
                    YOLO_SCRIPT_PATH,
                    "--weights", YOLO_MODEL_PATH,
                    "--source", INPUT_VIDEO_DIR,
                    "--project", OUTPUT_VIDEO_DIR,
                    "--name", "videoExp",
                    "--save-txt",  // 新增：保存检测结果
                    "--save-conf"   // 新增：保存置信度
            );
            pb.directory(new File("./backend/src/main/resources/python/yolov5"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取并打印脚本输出日志
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Video Detection] " + line);
                }
            }

            // 等待脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("YOLO视频检测失败，退出码：" + exitCode);
            }

            // 删除输入视频文件
            Files.list(Paths.get(INPUT_VIDEO_DIR)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 获取最新的输出目录expX
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

            // 解析检测结果的.txt文件
            Path labelsDir = outputExpDir.resolve("labels");
            if (Files.exists(labelsDir)) {
                Files.list(labelsDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".txt"))
                        .forEach(txtFile -> {
                            try {
                                List<String> lines = Files.readAllLines(txtFile);
                                for (String line : lines) {
                                    if (line.trim().isEmpty()) continue;

                                    // 解析检测结果
                                    String[] parts = line.split(" ");
                                    int classId = Integer.parseInt(parts[0]);
                                    double confidence = Double.parseDouble(parts[5]);

                                    // 生成逻辑主键（与图片检测保持一致）
                                    Long vehicleId = (long) (classId + 1);

                                    // 保存车辆记录
                                    Vehicle vehicle = vehicleService.getById(vehicleId);
                                    if (vehicle == null) {
                                        vehicle = new Vehicle();
                                        vehicle.setVehicleId(vehicleId);
                                        vehicle.setType(getTypeNameByClassId(classId));
                                        vehicle.setLicence(null); // 设置为null避免唯一约束冲突
                                        vehicleService.save(vehicle);
                                    }

                                    // 保存检测记录
                                    NonRealTimeDetectionRecord record = new NonRealTimeDetectionRecord();
                                    record.setUserId(3L);
                                    record.setTime(LocalDateTime.now());
                                    record.setConfidence(confidence);
                                    record.setVehicleId(vehicleId);
                                    record.setVehicleStatus("Nah");
                                    record.setMaxAge(24L);
                                    record.setExp(outputExpDir.getFileName().toString());

                                    nonRealTimeService.save(record);
                                }
                            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        });
            }

            // 返回检测结果标识
            return ResponseEntity.ok("视频检测完成，结果已保存" + outputExpDir.getFileName().toString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("视频处理失败: " + e.getMessage());
        }
    }
    /**
     * POST /api/yolo/image
     * 上传图片文件，经 Python 脚本检测后返回处理过的图片
     */
    private static final String INPUT_IMAGE_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbImages/input").getAbsolutePath();
    private static final String OUTPUT_IMAGE_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbImages/output").getAbsolutePath();

    @Autowired
    private NonRealTimeDetectionRecordService nonRealTimeService;
    @Autowired
    private VehicleService vehicleService;

    @PostMapping(value = "/image")
    public ResponseEntity<String> detectImage(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) throws Exception {
        System.out.println("Content-Type: " + request.getContentType());

        try {
            // 保存上传的图片到输入目录
            for (MultipartFile file : files) {
                String uniqueID = UUID.randomUUID().toString();
                Path inputImage = Paths.get(INPUT_IMAGE_DIR, "input_" + uniqueID + ".jpg");
                Files.write(inputImage, file.getBytes());
            }

            // 构造调用 Python 脚本命令
            ProcessBuilder pb = new ProcessBuilder(
                    CONDA_PYTHON_PATH,
                    YOLO_SCRIPT_PATH,
                    "--weights", YOLO_MODEL_PATH,
                    "--source", INPUT_IMAGE_DIR,
                    "--project", OUTPUT_IMAGE_DIR,
                    "--name", "imageExp",
                    "--save-txt",  // 保存检测结果为 .txt 文件
                    "--save-conf"  // 在 .txt 文件中保存置信度
            );
            pb.directory(new File("./backend/src/main/resources/python/yolov5"));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 输出脚本运行期间的日志信息
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("YOLO 图像检测脚本执行失败，退出码：" + exitCode);
            }

            // 删除输入目录中的图片
            Files.list(Paths.get(INPUT_IMAGE_DIR)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 获取最新的 expX 文件夹
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
                    .orElseThrow(() -> new RuntimeException("No output exp directory found"));

            // 获取 labels 子目录路径
            Path labelsDir = outputExpDir.resolve("labels");
            System.out.println("正在访问 labels 目录: " + labelsDir.toAbsolutePath());
            if (!Files.exists(labelsDir)) {
                System.out.println("警告: 未找到 labels 目录 - " + labelsDir);
                return ResponseEntity.ok("检测完成但未发现目标对象");
            }
            try {
                System.out.println("目录中的文件: " + Files.list(labelsDir).map(Path::getFileName).collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获取 exp 文件夹名称
            String expFolderName = outputExpDir.getFileName().toString();

// 解析检测结果的 .txt 文件（现在从 labels 子目录读取）
            Files.list(labelsDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(txtFile -> {
                        System.out.println("处理文件: " + txtFile.getFileName());
                        try {
                            List<String> lines = Files.readAllLines(txtFile);
                            System.out.println("文件内容: " + lines);

                            for (String line : lines) {
                                if (line.trim().isEmpty()) {
                                    System.out.println("跳过空行");
                                    continue;
                                }

                                // 解析每一行检测结果
                                String[] parts = line.split(" ");
                                int classId = Integer.parseInt(parts[0]);  // 类别 ID
                                double confidence = Double.parseDouble(parts[5]);  // 置信度

                                // 生成逻辑主键（classId+1）
                                Long vehicleId = (long) (classId + 1);

                                // 根据主键查询车辆记录
                                Vehicle vehicle = vehicleService.getById(vehicleId);

                                // 如果不存在则创建新记录
                                if (vehicle == null) {
                                    vehicle = new Vehicle();
                                    vehicle.setVehicleId(vehicleId); // 手动设置主键
                                    vehicle.setType(getTypeNameByClassId(classId));
                                    vehicle.setLicence(null);
                                    vehicleService.save(vehicle);
                                    System.out.println("Saved Vehicle: " + vehicleId);
                                }

                                // 创建检测记录
                                NonRealTimeDetectionRecord record = new NonRealTimeDetectionRecord();
                                record.setUserId(3L);
                                record.setTime(LocalDateTime.now());
                                record.setConfidence(confidence);
                                record.setVehicleId(vehicleId); // 直接使用计算出的主键
                                record.setVehicleStatus("Nah");
                                record.setMaxAge(24L);
                                record.setExp(expFolderName);

                                nonRealTimeService.save(record);
                                System.out.println("Saved Record: " + record.getNrdId());
                            }
                        } catch (IOException | ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            System.err.println("文件解析失败: " + txtFile + " | 错误: " + e.getMessage());
                        }
                    });

            // 返回 exp 文件夹名称
            return ResponseEntity.ok(expFolderName);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Internal server erpror: " + e.getMessage());
        }
    }

    private String getTypeNameByClassId(int classId) {
        switch (classId) {
            case 0: return "person";
            case 1: return "bicycle";
            case 2: return "car";
            case 3: return "motorcycle";
            case 4: return "airplane";
            case 5: return "bus";
            case 6: return "train";
            case 7: return "truck";
            case 8: return "boat";
            case 9: return "traffic light";
            case 10: return "fire hydrant";
            case 11: return "stop sign";
            case 12: return "parking meter";
            case 13: return "bench";
            case 14: return "bird";
            case 15: return "cat";
            case 16: return "dog";
            case 17: return "horse";
            case 18: return "sheep";
            case 19: return "cow";
            case 20: return "elephant";
            case 21: return "bear";
            case 22: return "zebra";
            case 23: return "giraffe";
            case 24: return "backpack";
            case 25: return "umbrella";
            case 26: return "handbag";
            case 27: return "tie";
            case 28: return "suitcase";
            case 29: return "frisbee";
            case 30: return "skis";
            case 31: return "snowboard";
            case 32: return "sports ball";
            case 33: return "kite";
            case 34: return "baseball bat";
            case 35: return "baseball glove";
            case 36: return "skateboard";
            case 37: return "surfboard";
            case 38: return "tennis racket";
            case 39: return "bottle";
            case 40: return "wine glass";
            case 41: return "cup";
            case 42: return "fork";
            case 43: return "knife";
            case 44: return "spoon";
            case 45: return "bowl";
            case 46: return "banana";
            case 47: return "apple";
            case 48: return "sandwich";
            case 49: return "orange";
            case 50: return "broccoli";
            case 51: return "carrot";
            case 52: return "hot dog";
            case 53: return "pizza";
            case 54: return "donut";
            case 55: return "cake";
            case 56: return "chair";
            case 57: return "couch";
            case 58: return "potted plant";
            case 59: return "bed";
            case 60: return "dining table";
            case 61: return "toilet";
            case 62: return "tv";
            case 63: return "laptop";
            case 64: return "mouse";
            case 65: return "remote";
            case 66: return "keyboard";
            case 67: return "cell phone";
            case 68: return "microwave";
            case 69: return "oven";
            case 70: return "toaster";
            case 71: return "sink";
            case 72: return "refrigerator";
            case 73: return "book";
            case 74: return "clock";
            case 75: return "vase";
            case 76: return "scissors";
            case 77: return "teddy bear";
            case 78: return "hair drier";
            case 79: return "toothbrush";
            default: return "unknown";
        }
    }
}