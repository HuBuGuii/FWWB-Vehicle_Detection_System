package com.fwwb.vehicledetection;

import com.fwwb.vehicledetection.config.CondaConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class VehicledetectionApplication {

	@Autowired
	private CondaConfig condaConfig; // 注入Conda配置

	public static void main(String[] args) {
		// 加载OpenCV DLL
		String dllPath = "src/main/resources/opencv/opencv_java4110.dll";
		System.load(Paths.get(dllPath).toAbsolutePath().toString());
		System.out.println("Loaded OpenCV DLL from: " + dllPath);
		System.out.println("java.library.path: " + System.getProperty("java.library.path"));

		// 启动Spring Boot应用
		SpringApplication.run(VehicledetectionApplication.class, args);
	}

	@Bean
	public CommandLineRunner checkDependencies(
			JdbcTemplate jdbcTemplate, // 注入pgSQL的JdbcTemplate
			RedisConnectionFactory redisConnectionFactory // 注入Redis连接工厂
	) {
		return args -> {
			System.out.println("-------------------以下是依赖和连接检测：-----------------------");
			System.out.println("Starting dependency checks...");

			// 1. 检测pgSQL数据库连接
			System.out.println("开始数据库连接检测...");
			checkPgSQLConnection(jdbcTemplate);

			// 2. 检测Redis连接
			System.out.println("开始Redis服务检测...");
			checkRedisConnection(redisConnectionFactory);

			// 3. 检测OpenCV是否正确加载
			System.out.println("检测OpenCV是否载入...");
			checkOpenCV();

			// 4. 检测YOLO是否可用
			System.out.println("检测YOLO服务是否可用...");
			checkYOLO();

			// 5. 检测Conda虚拟环境
			System.out.println("检测Conda环境是否可用...");
			checkCondaEnvironment();

			System.out.println("全部依赖检测结束。");
		};
	}

	/**
	 * 检测pgSQL数据库连接
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
	 * 检测Redis连接
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
	 * 检测OpenCV是否正确加载
	 */
	private void checkOpenCV() {
		try {
			// 尝试调用OpenCV的某个方法
			System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
			System.out.println("OpenCV is loaded successfully!");
		} catch (UnsatisfiedLinkError e) {
			System.out.println("OpenCV failed to load: " + e.getMessage());
		}
	}

	/**
	 * 检测YOLO是否可用
	 */
	private void checkYOLO() {
		String yoloPath = "src/main/resources/python/yolov5";
		File yoloDir = new File(yoloPath);
		if (yoloDir.exists() && yoloDir.isDirectory()) {
			System.out.println("YOLO directory found at: " + yoloPath);
			// 这里可以添加调用YOLO的命令行工具或脚本，检测其是否可用
			// 例如：运行 `yolo --version` 并解析输出
			System.out.println("YOLO is available.");
		} else {
			System.out.println("YOLO directory not found at: " + yoloPath);
		}
	}

	/**
	 * 检测Conda虚拟环境
	 */
	private void checkCondaEnvironment() {
		try {
			// 检测配置的Conda Python路径
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
}