package com.t13max.cc.table;

import com.t13max.cc.ClumsyCatEngine;
import com.t13max.cc.bean.AutoData;
import com.t13max.cc.bean.IData;
import com.t13max.cc.cache.ITableCache;
import com.t13max.cc.cache.TableCacheFactory;
import com.t13max.cc.conf.CacheConf;
import com.t13max.cc.conf.ClumsyCatConf;
import com.t13max.cc.conf.TableConf;
import com.t13max.cc.storage.IStorage;
import lombok.val;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 所有表的集合
 *
 * @Author t13max
 * @Date 14:44 2025/7/15
 */
public class Tables {

    private final static Tables tables = new Tables();

    private final Map<String, Table<? extends IData>> tablesMap = new HashMap<>();

    private final AtomicBoolean startMark = new AtomicBoolean();

    private Tables() {

    }

    public static Tables inst() {
        return tables;
    }

    public boolean start(ClumsyCatConf conf) throws Exception {
        //根据配置创建
        if (!startMark.compareAndSet(false, true)) {
            return false;
        }
        CacheConf cacheConf = conf.getCache();

        String tableCacheInstance = cacheConf.getInstance();
        boolean cacheOpen = cacheConf.isOpen();

        //根据配置创建..
        String tablePath = conf.getTablePath();
        List<TableConf> tableConfList = conf.getTables();
        for (TableConf tableConf : tableConfList) {
            Class<?> clazz = Class.forName(tablePath + "." + tableConf.getName());
            if (!Table.class.isAssignableFrom(clazz)) {
                continue;
            }
            //表构造器
            Constructor<?> constructor = clazz.getDeclaredConstructor(TableConf.class, ITableCache.class, IStorage.class);
            //表缓存
            ITableCache<IData> tableCache = cacheOpen ? TableCacheFactory.createTableCache(tableCacheInstance) : TableCacheFactory.createTableCache(null);
            //表实例
            Object instance = constructor.newInstance(tableConf, tableCache, ClumsyCatEngine.inst().getStorage());
            if (!(instance instanceof Table<?> table)) {
                continue;
            }
            tablesMap.put(tableConf.getName(), table);
        }

        return true;
    }

    //临时方案 理论上应该在start里自动扫描 添加
    public void putTable(String name, Table<? extends IData> table) {
        this.tablesMap.put(name, table);
    }

    //停止调用
    public boolean shutdown() {

        return false;
    }

    public <T extends AutoData> Table<T> getTable(String tableName) {

        return (Table<T>) tablesMap.get(tableName);
    }
}
