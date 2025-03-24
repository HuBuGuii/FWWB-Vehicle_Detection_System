// File: src/main/java/com/fwwb/vehicledetection/util/TokenUtil.java
package com.fwwb.vehicledetection.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class TokenUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RedisScript<Long> luaScript;

    @PostConstruct
    public void init() {
        String script = "if redis.call('exists', KEYS[1]) == 1 then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        // 直接通过构造函数创建 RedisScript
        luaScript = new DefaultRedisScript<>(script, Long.class);
    }

    public boolean verifyToken(String token) {
        String key = "token:" + token;
        Long result = redisTemplate.execute(luaScript, Collections.singletonList(key));
        return result != null && result > 0;
    }

    public void storeToken(String token, long expireSeconds) {
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
    }
}