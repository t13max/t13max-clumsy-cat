package com.t13max.kdb.cache;

import com.t13max.kdb.bean.IData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表缓存默认实现
 *
 * @author t13max
 * @since 10:52 2025/7/8
 */
public class DefaultTableCache<V extends IData> extends AbstractTableCache<V> {

    private final Map<Long, V> cacheMap = new LinkedHashMap<>();

    @Override
    public V get(long id) {
        return null;
    }

    @Override
    public void add(V record) {

    }

    @Override
    public V remove(long id) {
        return null;
    }

    @Override
    public void clear(){

    }

    @Override
    public void clean(){

    }
}
