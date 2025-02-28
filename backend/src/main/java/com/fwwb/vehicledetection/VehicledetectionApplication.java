package com.fwwb.vehicledetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class VehicledetectionApplication {
	public static void main(String[] args) {
		String dllPath = "D:\\Work\\Tools\\opencv\\opencv_4.11.0\\opencv\\build\\java\\x64\\opencv_java4110.dll";
		System.load(dllPath);
		System.out.println("Loaded OpenCV DLL from: " + dllPath);
		System.out.println("java.library.path: " + System.getProperty("java.library.path"));
		SpringApplication.run(VehicledetectionApplication.class, args);
	}
}


