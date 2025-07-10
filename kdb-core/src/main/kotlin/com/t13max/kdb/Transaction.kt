package com.t13max.kdb

import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.utils.CoroutineLocal
import kotlin.coroutines.coroutineContext

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

        suspend fun current(): Transaction {
            return transactionLocal.get()
        }

        suspend fun create(): Transaction {
            var transaction = current()
            if (transaction == null) {
                val ctx = coroutineContext
                transaction = Transaction()
                transactionLocal.update(transaction, ctx)
            }
            return transaction
        }

        suspend fun destroy() {
            val ctx = coroutineContext
            transactionLocal.update(null, ctx)
        }
    }

    //事务缓存


    //添加持有的锁
    suspend fun addLock(lock: RecordLock) {

    }

    private fun finish() {

    }


    suspend fun perform(procedure: Procedure) {

    }

    suspend fun rollback() {

    }

    suspend fun commit() {

    }
}