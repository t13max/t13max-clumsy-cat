package com.t13max.kdb.conf;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
public class BeanConf {

    //Bean名
    public final String name;

    //说明
    public final String comment;

    //变量配置 变量名->配置
    public final Map<String, VarConf> varConfMap;

    @ConstructorProperties({"name", "var", "comment"})
    public BeanConf(String name, List<VarConf> varConfList, String comment) {
        this.name = name;
        this.comment = comment;
        this.varConfMap = varConfList.stream().collect(Collectors.toUnmodifiableMap(varConf -> varConf.name, varConf -> varConf));

    }
}
