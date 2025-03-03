// File: src/main/java/com/fwwb/vehicledetection/controller/detection/YoloDetectionController.java
package com.fwwb.vehicledetection.controller.yolo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/yolo")
public class YoloDetectionController {

    @Value("${yolo.script.path:${user.dir}/backend/src/main/resources/python/yolo_video_detection.py}")
    private String yoloScriptPath;

    @Value("${yolo.image.script.path:${user.dir}/backend/src/main/resources/python/yolo_image_detection.py}")
    private String yoloImageScriptPath;

    @Value("${yolo.model.path:model.pt}")
    private String yoloModelPath;

    @Value("${yolo.output.dir:/tmp}")
    private String yoloOutputDir;

    /**
     * POST /detection/video
     * 上传视频文件，经 Python 脚本检测后返回处理过的视频
     */

    @PostMapping("/video")
    public ResponseEntity<byte[]> detectVideo(@RequestParam("file") MultipartFile file) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueID = UUID.randomUUID().toString();
        Path inputVideo = Paths.get(tempDir, "input_" + uniqueID + ".mp4");
        Path outputVideo = Paths.get(tempDir, "output_" + uniqueID + ".mp4");

        try {
            Files.write(inputVideo, file.getBytes());

            ProcessBuilder pb = new ProcessBuilder("python", yoloScriptPath,
                    "--input", inputVideo.toString(),
                    "--output", outputVideo.toString(),
                    "--model", yoloModelPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(500)
                        .body(("YOLO 检测脚本执行失败，退出码：" + exitCode).getBytes());
            }

            byte[] videoBytes = Files.readAllBytes(outputVideo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("video/mp4"));
            headers.setContentDispositionFormData("attachment", "output_" + uniqueID + ".mp4");

            return ResponseEntity.ok().headers(headers).body(videoBytes);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage().getBytes());
        } finally {
            try {
                Files.deleteIfExists(inputVideo);
                Files.deleteIfExists(outputVideo);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * POST /api/yolo/image
     * 上传图片文件，经 Python 脚本检测后返回处理过的图片
     */
    private static final String YOLO_SCRIPT_PATH = "detect.py"; // 改为相对路径
    private static final String YOLO_MODEL_PATH = "yolov5s.pt"; // 改为相对路径
    private static final String INPUT_IMAGE_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbImages/input").getAbsolutePath();
    private static final String OUTPUT_IMAGE_DIR = new File("./backend/src/main/resources/python/yolov5/fwwbImages/output").getAbsolutePath();
    private static final String CONDA_PYTHON_PATH = "D:/Work/Tools/Anaconda/envs/yolov5/python.exe";

    @PostMapping(value = "/image")
    public ResponseEntity<byte[]> detectImage(MultipartFile[] files, HttpServletRequest request) throws Exception {
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
                    "--name", "exp"
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
                    .filter(path -> path.getFileName().toString().startsWith("exp"))
                    .max(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                    .orElseThrow(() -> new RuntimeException("No output exp directory found"));

            // 创建 ZIP 文件
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                // 遍历 expX 文件夹中的所有图片文件
                for (Path imagePath : Files.list(outputExpDir).filter(Files::isRegularFile).toList()) {
                    // 创建 ZIP 条目
                    ZipEntry zipEntry = new ZipEntry(imagePath.getFileName().toString());
                    zipOut.putNextEntry(zipEntry);
                    // 将图片文件写入 ZIP
                    Files.copy(imagePath, zipOut);
                    zipOut.closeEntry();
                }
            }

            // 返回 ZIP 文件
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "results.zip");
            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Internal server error: " + e.getMessage());
        }
    }
}