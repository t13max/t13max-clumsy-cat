package com.t13max.kdb.conf;

import java.beans.ConstructorProperties;

/**
 * 变量
 *
 * @author t13max
 * @since 14:24 2025/7/15
 */
public class VarConf {

    //变量名
    public final String name;

    //变量类型
    public final String type;

    //说明
    public final String comment;

    @ConstructorProperties({"name", "type", "comment"})
    public VarConf(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }
}
