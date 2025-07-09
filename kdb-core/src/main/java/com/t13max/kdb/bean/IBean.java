package com.t13max.kdb.bean;

/**
 * @author t13max
 * @since 16:03 2025/7/7
 */
public interface IBean {

    Long getId();

    IBean parent();

    String getVarName();


}
