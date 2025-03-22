package com.fwwb.vehicledetection.controller.yolo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/download")
public class YoloDownloadController {

    // 视频检测：输出目录
    private static final String OUTPUT_VIDEO_DIR = new File("./src/main/resources/yolo/image/videoOutput").getAbsolutePath();
    // 图像检测：输出目录
    private static final String OUTPUT_IMAGE_DIR = new File("./src/main/resources/yolo/image/imageOutput").getAbsolutePath();

    @GetMapping("/image/{fileName}")
    public void downloadImage(@PathVariable String fileName, HttpServletResponse response) {
        String filePath = OUTPUT_IMAGE_DIR + File.separator + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".zip\"");
                zipFile(file, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @GetMapping("/video/{fileName}")
    public void downloadVideo(@PathVariable String fileName, HttpServletResponse response) {
        String filePath = OUTPUT_VIDEO_DIR + File.separator + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".zip\"");
                zipFile(file, response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void zipFile(File file, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zipOut);
            } else {
                zipFile(file, file.getName(), zipOut);
            }
        }
    }

    private void zipDirectory(File directory, String baseName, ZipOutputStream zipOut) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectory(file, baseName + "/" + file.getName(), zipOut);
                } else {
                    zipFile(file, baseName + "/" + file.getName(), zipOut);
                }
            }
        }
    }

    private void zipFile(File file, String fileName, ZipOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}