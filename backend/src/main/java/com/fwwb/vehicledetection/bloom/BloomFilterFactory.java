// File: src/main/java/com/fwwb/vehicledetection/bloom/BloomFilterFactory.java
package com.fwwb.vehicledetection.bloom;

import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import java.nio.charset.StandardCharsets;

public class BloomFilterFactory {

    /**
     * 获取布隆过滤器实例
     *
     * @param type 布隆过滤器的业务类型（目前只支持 "default"）
     * @param expectedInsertions 预期插入数量
     * @param fpp 误判率
     * @param funnel 元素序列化策略（若为 null，则默认使用 String Funnel）
     * @return BloomFilterStrategy 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> BloomFilterStrategy<T> getBloomFilter(String type, int expectedInsertions, double fpp, Funnel<T> funnel) {
        if ("default".equalsIgnoreCase(type)) {
            if (funnel == null) {
                // 默认使用 UTF-8 字符串 Funnel，当 T 为 String 类型时适用
                funnel = (Funnel<T>) Funnels.stringFunnel(StandardCharsets.UTF_8);
            }
            return new DefaultBloomFilterStrategy<>(expectedInsertions, fpp, funnel);
        }
        throw new IllegalArgumentException("Unknown Bloom Filter type: " + type);
    }
}