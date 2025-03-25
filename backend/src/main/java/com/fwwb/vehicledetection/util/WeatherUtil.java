package com.fwwb.vehicledetection.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WeatherUtil {
    private static volatile String currentTemperature = "25°C"; // 默认值
    private static volatile String currentWeather = "晴";       // 默认天气

    static {
        // 使用定时任务每小时更新一次天气数据（实际可调整为每分钟或根据 API 限制）
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            // 此处应调用免费的天气 API 获取最新数据
            currentTemperature = fetchTemperatureFromAPI();
            currentWeather = fetchWeatherFromAPI();
            System.out.println("更新天气数据：" + currentTemperature + ", " + currentWeather);
        }, 0, 1, TimeUnit.HOURS);
    }

    private static String fetchTemperatureFromAPI() {
        // 模拟调用 API，返回温度数据
        return "25°C";
    }

    private static String fetchWeatherFromAPI() {
        // 模拟调用 API，返回天气状况
        return "晴";
    }

    /**
     * 返回包含最新天气数据的 Map，键为 "temperature" 与 "weather"
     */
    public static Map<String, Object> getCurrentWeather() {
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("temperature", currentTemperature);
        weatherData.put("weather", currentWeather);
        return weatherData;
    }
}