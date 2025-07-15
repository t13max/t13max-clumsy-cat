package com.t13max.kdb.table;

import com.t13max.kdb.bean.IData;
import com.t13max.kdb.conf.KdbConf;

import java.util.*;

/**
 * 所有表的集合
 *
 * @Author t13max
 * @Date 14:44 2025/7/15
 */
public class Tables {

    private final Map<String, Table<? extends IData>> tables = new HashMap<>();

    public Tables(KdbConf kdbConf) {

        //创建表 表里有表缓存 数据存储层建立连接
    }

    public boolean shutdown() {

        return false;
    }
}
