package com.t13max.cc.table;

import com.t13max.cc.bean.IData;
import com.t13max.cc.conf.ClumsyCatConf;

import java.util.HashMap;
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

    public boolean start(ClumsyCatConf conf) {
        //根据配置创建
        if (!startMark.compareAndSet(false, true)) {
            return false;
        }

        //根据配置创建..

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

    public <T extends IData> Table<T> getTable(String tableName) {

        return (Table<T>) tablesMap.get(tableName);
    }
}
