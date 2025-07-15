package com.t13max.kdb.conf;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kdb配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
public class KdbConf {

    public final int a;

    public final int b;

    //表配置 表名->配置
    public final Map<String, TableConf> tableConfMap;

    //Bean配置 Bean名->配置
    public final Map<String, BeanConf> beanConfMap;

    @ConstructorProperties({"a", "b", "table", "bean"})
    public KdbConf(int a, int b, List<TableConf> tableConfList, List<BeanConf> beanConfList) {
        this.a = a;
        this.b = b;
        this.tableConfMap = tableConfList.stream().collect(Collectors.toUnmodifiableMap(tableConf -> tableConf.name, tableConf -> tableConf));
        this.beanConfMap = beanConfList.stream().collect(Collectors.toUnmodifiableMap(tableConf -> tableConf.name, tableConf -> tableConf));
    }
}
