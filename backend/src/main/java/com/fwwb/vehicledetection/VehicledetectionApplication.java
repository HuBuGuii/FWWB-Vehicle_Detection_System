package com.fwwb.vehicledetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VehicledetectionApplication {
	public static void main(String[] args) {
		// 启动 Spring Boot 应用
		SpringApplication.run(VehicledetectionApplication.class, args);
	}
}