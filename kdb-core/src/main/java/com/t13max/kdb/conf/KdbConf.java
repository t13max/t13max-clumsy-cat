package com.t13max.kdb.conf;

import lombok.Data;

import java.util.Map;

/**
 * Kdb配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class KdbConf {


    private int a;

    private int b;

    //表配置 表名->配置
    private Map<String, TableConf> tableConfMap;

    //Bean配置 Bean名->配置
    private Map<String, BeanConf> beanConfMap;
}
