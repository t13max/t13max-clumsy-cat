package com.t13max.cc.cache;

import com.t13max.cc.ClumsyCatEngine;
import com.t13max.cc.bean.IData;
import com.t13max.cc.conf.CacheConf;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表缓存默认实现
 *
 * @author t13max
 * @since 10:52 2025/7/8
 */
public class DefaultTableCache<V extends IData> extends AbstractTableCache<V> {

    private final Map<Long, AccessTimeValue<V>> cacheMap = new ConcurrentHashMap<>();

    @Override
    public V get(long id) {
        AccessTimeValue<V> accessTimeValue = cacheMap.get(id);
        if (accessTimeValue == null) {
            return null;
        }
        return accessTimeValue.value;
    }

    @Override
    public void add(V value) {
        long currentTimeMillis = System.currentTimeMillis();
        cacheMap.compute(value.getId(), (k, v) -> {
            if (v == null) {
                // 不存在 新建
                return new AccessTimeValue<>(currentTimeMillis, value);
            } else {
                // 已存在 更新字段
                v.accessMills = currentTimeMillis;
                return v;
            }
        });
    }

    @Override
    public V remove(long id) {
        AccessTimeValue<V> accessTimeValue = cacheMap.remove(id);
        if (accessTimeValue == null) {
            return null;
        }

        return accessTimeValue.value;
    }

    @Override
    public void clear() {
        this.cacheMap.clear();
    }

    @Override
    public void clean() {
        long currentTimeMillis = System.currentTimeMillis();
        CacheConf cacheConf = ClumsyCatEngine.inst().getConf().getCache();
        int expired = cacheConf.getExpired();
        Iterator<AccessTimeValue<V>> iterator = this.cacheMap.values().iterator();
        while (iterator.hasNext()) {
            AccessTimeValue<V> accessTimeValue = iterator.next();
            if (currentTimeMillis - accessTimeValue.accessMills <= expired) {
                continue;
            }
            if (tryRemoveValue(accessTimeValue.value)) {
                iterator.remove();
            }
        }
    }

    private static class AccessTimeValue<V> {
        long accessMills;
        V value;

        public AccessTimeValue(long accessMills, V value) {
            this.accessMills = accessMills;
            this.value = value;
        }
    }
}

