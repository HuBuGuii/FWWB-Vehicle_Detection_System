// File: src/main/java/com/fwwb/vehicledetection/util/TokenUtil.java
package com.fwwb.vehicledetection.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class TokenUtil {



    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<Long> luaScript;

    @PostConstruct
    public void init() {
        // Lua 脚本：检查 key 是否存在，若存在则删除，并返回删除的数量
        String script = "if redis.call('exists', KEYS[1]) == 1 then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(script);
        luaScript.setResultType(Long.class);
    }

    /**
     * 验证令牌是否存在且仅使用一次
     * @param token idempotency token
     * @return 如果存在则删除返回 true，否则返回 false
     */
    public boolean verifyToken(String token) {
        // 存储 token 的 key 约定为 "token:{token}"
        String key = "token:" + token;
        Long result = redisTemplate.execute(luaScript, Collections.singletonList(key));
        return result != null && result > 0;
    }

    /**
     * 存储令牌（用于后续验证），设置过期时间（秒）
     *
     * @param token idempotency token
     * @param expireSeconds token 的过期时间（单位秒）
     */
    public void storeToken(String token, long expireSeconds) {
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.MINUTES);
    }
}