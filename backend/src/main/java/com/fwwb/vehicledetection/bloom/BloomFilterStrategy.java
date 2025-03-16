// File: src/main/java/com/fwwb/vehicledetection/bloom/BloomFilterStrategy.java
package com.fwwb.vehicledetection.bloom;

public interface BloomFilterStrategy<T> {
    /**
     * 添加元素到布隆过滤器。
     * @param item 元素
     * @return 如果元素已经可能存在，则返回 true；否则返回 false（新增后结果可能为 true）
     */
    boolean add(T item);

    /**
     * 检查元素是否存在。
     * @param item 元素
     * @return 如果可能存在，则返回 true；如果一定不存在，则返回 false。
     */
    boolean contains(T item);
}