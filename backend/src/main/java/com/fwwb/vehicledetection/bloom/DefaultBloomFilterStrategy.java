// File: src/main/java/com/fwwb/vehicledetection/bloom/DefaultBloomFilterStrategy.java
package com.fwwb.vehicledetection.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import java.nio.charset.StandardCharsets;

public class DefaultBloomFilterStrategy<T> implements BloomFilterStrategy<T> {

    private final BloomFilter<T> bloomFilter;

    /**
     * 构造方法
     * @param expectedInsertions 预估插入量
     * @param fpp 允许的误判率
     * @param funnel 用于序列化元素的 Funnel
     */
    public DefaultBloomFilterStrategy(int expectedInsertions, double fpp, Funnel<T> funnel) {
        this.bloomFilter = BloomFilter.create(funnel, expectedInsertions, fpp);
    }

    @Override
    public boolean add(T item) {
        // Guava 的 put() 方法：如果元素可能已存在返回 false，
        // 这里先检查，再放入，返回先前是否存在的结果
        boolean alreadyPresent = bloomFilter.mightContain(item);
        bloomFilter.put(item);
        return alreadyPresent;
    }

    @Override
    public boolean contains(T item) {
        return bloomFilter.mightContain(item);
    }
}