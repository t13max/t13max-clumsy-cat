package com.t13max.kdb.consts;

/**
 * 数据状态
 *
 * @author t13max
 * @since 17:02 2025/7/10
 */
public enum State {

    INDB_GET, // 查询装载进入
    INDB_REMOVE, // 删除，在数据库中存在该记录。
    INDB_ADD, // 新增记录，在数据库中存在该记录。这种情况原因：删除操作还没有flush，又执行了增加。
    ADD, // 新增的记录
    REMOVE, // 新增操作还没有flush，又执行了删除。此状态记录只存在于事务过程中，事务结束就会从cache中删除。
}

