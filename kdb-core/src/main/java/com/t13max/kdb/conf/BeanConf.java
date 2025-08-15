package com.t13max.kdb.conf;

import lombok.Data;

import java.util.Map;

/**
 * Bean配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class BeanConf {

    //Bean名
    private String name;

    //说明
    private String comment;

    //变量配置 变量名->配置
    private Map<String, VarConf> varConfMap;

}
