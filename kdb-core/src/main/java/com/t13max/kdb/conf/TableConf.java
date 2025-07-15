package com.t13max.kdb.conf;

import java.beans.ConstructorProperties;

/**
 * 表配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
public class TableConf {

    //表名
    public final String name;
    //表里存值 就是Bean的名字
    public final String value;
    //说明
    public final String comment;
    //修改这个数据所需要持有的锁
    public final String lock;

    @ConstructorProperties({"name", "value", "comment", "lock"})
    public TableConf(String name, String value, String comment, String lock) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.lock = lock;
    }
}
