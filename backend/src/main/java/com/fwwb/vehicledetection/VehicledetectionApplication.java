package com.fwwb.vehicledetection;

import com.fwwb.vehicledetection.config.CondaConfig;
import org.bytedeco.javacpp.Loader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


@SpringBootApplication
@EnableScheduling
public class VehicledetectionApplication {

	@Autowired
	private CondaConfig condaConfig;

	public static void main(String[] args) {
		// 启动 Spring Boot 应用
		SpringApplication.run(VehicledetectionApplication.class, args);
	}

	@Bean
	public CommandLineRunner checkDependencies(
			JdbcTemplate jdbcTemplate, // 注入 pgSQL 的 JdbcTemplate
			RedisConnectionFactory redisConnectionFactory // 注入 Redis 连接工厂
	) {
		return args -> {
			System.out.println("-------------------以下是依赖和连接检测：-----------------------");
			System.out.println("Starting dependency checks...");

			// 1. 检测 pgSQL 数据库连接
			System.out.println("开始数据库连接检测...");
			checkPgSQLConnection(jdbcTemplate);

			// 2. 检测 Redis 连接
			System.out.println("开始 Redis 服务检测...");
			checkRedisConnection(redisConnectionFactory);

			// 3. 检测 OpenCV 是否正确加载
			System.out.println("检测 OpenCV 是否载入...");
			checkOpenCV();

			// 4. 检测 Conda 虚拟环境
			System.out.println("检测 Conda 环境是否可用...");
			checkCondaEnvironment();

			System.out.println("全部依赖检测结束。");
		};
	}

	/**
	 * 检测 pgSQL 数据库连接
	 */
	private void checkPgSQLConnection(JdbcTemplate jdbcTemplate) {
		try {
			jdbcTemplate.execute("SELECT 1");
			System.out.println("pgSQL connection is OK!");
		} catch (Exception e) {
			System.out.println("pgSQL connection failed: " + e.getMessage());
		}
	}

	/**
	 * 检测 Redis 连接
	 */
	private void checkRedisConnection(RedisConnectionFactory redisConnectionFactory) {
		try {
			redisConnectionFactory.getConnection().ping();
			System.out.println("Redis connection is OK!");
		} catch (Exception e) {
			System.out.println("Redis connection failed: " + e.getMessage());
		}
	}

	/**
	 * 检测 OpenCV 是否正确加载
	 */
	private void checkOpenCV() {
		try {
			// 检查 OpenCV 版本
			String version = org.opencv.core.Core.VERSION;
			System.out.println("OpenCV loaded successfully! Version: " + version);
		} catch (UnsatisfiedLinkError e) {
			System.out.println("OpenCV native library failed to load: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("OpenCV check failed: " + e.getMessage());
		}
	}

	/**
	 * 检测 Conda 虚拟环境
	 */
	private void checkCondaEnvironment() {
		try {
			// 检测配置的 Conda Python 路径
			String pythonPath = condaConfig.getPath();
			File pythonFile = new File(pythonPath);
			if (pythonFile.exists()) {
				System.out.println("Conda Python path is valid: " + pythonPath);
			} else {
				System.out.println("Conda Python path is invalid: " + pythonPath);
			}
		} catch (Exception e) {
			System.out.println("Conda environment check failed: " + e.getMessage());
		}
	}

	private static class opencv_java {
	}
}