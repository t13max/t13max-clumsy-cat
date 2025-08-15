package com.t13max.kdb.conf;

import lombok.Data;

/**
 * 表配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class TableConf {

    //表名
    private String name;
    //表里存值 就是Bean的名字
    private String value;
    //说明
    private String comment;
    //修改这个数据所需要持有的锁
    private String lock;

}
