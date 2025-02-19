package com.fwwb.vehicledetection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class VehicledetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicledetectionApplication.class, args);
	}

}


