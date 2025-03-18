package com.fwwb.vehicledetection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "conda.python")
public class CondaConfig {
    private String path;

    // Getter and Setter
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}