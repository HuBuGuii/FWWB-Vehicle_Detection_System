package com.fwwb.vehicledetection.controller.roadMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/roadmap")
public class RoadMapController {
    private static final String ROAD_MAP_JSON = "classpath:roadMap/roadMap.json";

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/getJson")
    public void download(HttpServletResponse response) {
        try {
            Resource resource = resourceLoader.getResource(ROAD_MAP_JSON);
            if (resource.exists()) {
                response.setContentType("application/json");
                response.setHeader("Content-Disposition",
                        "attachment; filename=" + resource.getFilename());

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

    @PostMapping("/updateJson")
    public String update(
            @RequestParam("file") MultipartFile file,
            HttpServletResponse response
    ) {
        if (file.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "File is empty";
        }

        try {
            String uploadDir = "external-config/roadMap/";
            Files.createDirectories(Paths.get(uploadDir));

            File targetFile = new File(uploadDir + "roadMap.json");
            file.transferTo(targetFile);
            return "File updated successfully";
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Failed to update file";
        }
    }
}