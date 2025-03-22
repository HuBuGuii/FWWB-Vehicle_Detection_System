package com.fwwb.vehicledetection.controller.roadMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/api/roadmap")
public class RoadMapController {
    private static final String ROAD_MAP_JSON = "classpath:roadMap/roadMap.json";

    @Autowired
    private ResourceLoader resourceLoader;

    // 从路径得到Json地图文件，返回给客户端
    @GetMapping("/getJson")
    public void download(HttpServletResponse response) {
        try {
            Resource resource = resourceLoader.getResource(ROAD_MAP_JSON);
            if (resource.exists()) {
                response.setContentType("application/json");
                response.setHeader("Content-Disposition", "attachment; filename=" + resource.getFilename());

                try (InputStream inputStream = resource.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("File not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // 客户端上传新的Json地图文件，覆盖原有的Json地图文件
    @PostMapping("/updateJson")
    public String update(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "File is empty";
        }

        try {
            // 获取资源文件的绝对路径
            Resource resource = resourceLoader.getResource(ROAD_MAP_JSON);
            File targetFile = resource.getFile();
            file.transferTo(targetFile);
            return "File updated successfully";
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Failed to update file";
        }
    }
}