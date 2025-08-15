package com.t13max.kdb.bean;

/**
 * @author t13max
 * @since 16:59 2025/7/7
 */
public interface IData {

    long getId();

    IData parent();

    //提交
    default boolean commit() {
        return true;
    }

    //回滚
    default boolean rollBack() {
        return true;
    }
}
