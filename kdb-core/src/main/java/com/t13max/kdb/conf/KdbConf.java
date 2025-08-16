package com.t13max.kdb.conf;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Kdb配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
@Data
public class KdbConf {

    //是否开启缓存
    private boolean cache;

    //表配置 表名->配置
    private List<TableConf> tables;


}
