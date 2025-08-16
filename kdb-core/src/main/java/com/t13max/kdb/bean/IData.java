package com.t13max.kdb.bean;

/**
 * @author t13max
 * @since 16:59 2025/7/7
 */
public interface IData {

    long getId();

    //预留 未来支持引用类型
    default IData parent() {
        return null;
    }

    //提交
    default void commit() {
    }

    //回滚
    default void rollback() {
    }
}
