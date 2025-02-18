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


@Component
class DatabaseChecker implements CommandLineRunner {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) {
		try {
			int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables", Integer.class);
			System.out.println("数据库连接成功，表数量：" + count);
		} catch (Exception e) {
			System.err.println("数据库连接失败：" + e.getMessage());
		}
	}
}