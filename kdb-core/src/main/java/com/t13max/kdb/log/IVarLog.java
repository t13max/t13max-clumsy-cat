package com.t13max.kdb.log;

/**
 * 回滚日志顶级接口
 *
 * @Author t13max
 * @Date 15:54 2025/7/7
 */
public interface IVarLog<V> {

    //回滚
    void rollback();

    //提交
    void commit();

    //检查是否有变化
    boolean checkNoChange(V v);
}
