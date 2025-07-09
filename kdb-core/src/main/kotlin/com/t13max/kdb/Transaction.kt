package com.t13max.kdb

import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.utils.CoroutineLocal

/**
 * 事务
 *
 * @author t13max
 * @since 16:50 2025/7/9
 */
class Transaction {

    companion object {
        // 最大分段数
        private val transactionLocal = CoroutineLocal { Transaction() }


    }

    //事务缓存


    //添加持有的锁
    suspend fun addLock(lock: RecordLock) {

    }
}