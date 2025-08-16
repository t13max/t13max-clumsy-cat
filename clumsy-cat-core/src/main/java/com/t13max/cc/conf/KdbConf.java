package com.t13max.cc.conf;

import lombok.Data;

import java.util.List;

/**
 * Kdb配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class KdbConf {

    //缓存相关配置
    private CacheConf cache;

    //自动存库相关配置
    private AutoConf auto;

    //存储层配置
    private StorageConf storage;

    //实体类相关配置
    private DataConf data;

    //表配置 表名->配置
    private List<TableConf> tables;


}
