// File: src/main/java/com/fwwb/vehicledetection/controller/detection/YoloDetectionController.java
package com.fwwb.vehicledetection.controller.yolo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
     * POST /detection/image
     * 上传图片文件，经 Python 脚本检测后返回处理过的图片
     */
    @PostMapping(value = "/image")
    public ResponseEntity<byte[]> detectImage( @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        System.out.println("Content-Type: " + request.getContentType());
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueID = UUID.randomUUID().toString();
        // 使用 .jpg 扩展名保存文件
        Path inputImage = Paths.get(tempDir, "input_" + uniqueID + ".jpg");
        Path outputImage = Paths.get(tempDir, "output_" + uniqueID + ".jpg");

        try {
            // 将上传的图片保存到临时文件
            Files.write(inputImage, file.getBytes());

            // 构造调用 Python 脚本命令：yolo_image_detection.py
            ProcessBuilder pb = new ProcessBuilder("python", yoloImageScriptPath,
                    "--input", inputImage.toString(),
                    "--output", outputImage.toString(),
                    "--model", yoloModelPath);
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
                return ResponseEntity.status(500)
                        .body(("YOLO 图像检测脚本执行失败，退出码：" + exitCode).getBytes());
            }

            // 读取处理后的图片数据并返回
            byte[] imageBytes = Files.readAllBytes(outputImage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("attachment", "output_" + uniqueID + ".jpg");

            return ResponseEntity.ok().headers(headers).body(imageBytes);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage().getBytes());
        } finally {
            // 清理临时文件
            try {
                Files.deleteIfExists(inputImage);
                Files.deleteIfExists(outputImage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}