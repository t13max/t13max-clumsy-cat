package com.t13max.cc.cache;

import com.t13max.cc.bean.IData;

/**
 * 表缓空实现
 *
 * @author t13max
 * @since 10:52 2025/7/8
 */
public class EmptyTableCache<V extends IData> extends AbstractTableCache<V> {

    private final static EmptyTableCache<?> EMPTY_TABLE_CACHE = new EmptyTableCache<>();

    @SuppressWarnings("unchecked")
    public static <V extends IData> EmptyTableCache<V> emptyTableCache() {
        return (EmptyTableCache<V>)EMPTY_TABLE_CACHE;
    }

    @Override
    public V get(long id) {
        return null;
    }

    @Override
    public void add(V value) {

    }

    @Override
    public V remove(long id) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void clean() {

    }
}
