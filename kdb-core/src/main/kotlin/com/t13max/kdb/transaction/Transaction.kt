package com.t13max.kdb.transaction

import com.t13max.kdb.bean.IData
import com.t13max.kdb.bean.Record
import com.t13max.kdb.executor.AutoSaveExecutor
import com.t13max.kdb.lock.LockCache
import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.utils.CoroutineLocal
import com.t13max.kdb.utils.Log
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
    private val recordCache = mutableMapOf<Class<out IData>, MutableMap<Long, Record<out IData>>>()

    //当前事务持有的锁
    private val lockCache = mutableListOf<RecordLock>()

    //检查点
    private val savepoint = Savepoint()

    //添加持有的锁
    suspend fun addLock(lock: RecordLock) {

    }

    /**
     * 执行Procedure
     *
     * @Author t13max
     * @Date 11:02 2025/7/12
     */
    suspend fun perform(procedure: Procedure) {
        try {
            LockCache.flushLock().readLock()

            if (procedure.call()) {
                realCommit()
            } else {
                lastRollback()
            }
        } catch (throwable: Throwable) {
            Log.TRANSACT.error("perform exception! throwable={}", throwable)
            lastRollback()
        } finally {
            finish()
            LockCache.flushLock().readUnlock()
        }
    }

    /**
     * Procedure调用的 回滚
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun rollback() {

    }

    /**
     * Procedure调用的 提交
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun commit() {

    }

    /**
     * Transaction自己调用的 最后的提交
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun realCommit() {
        savepoint.commit()
    }

    /**
     * Transaction自己调用的 最后的回滚
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun lastRollback() {
        savepoint.rollback()
    }

    /**
     * 执行完 最终调用
     *
     * @Author t13max
     * @Date 11:03 2025/7/12
     */
    private fun finish() {
        //把事务缓存里的数据 变动的 扔进异步存库

        val recordList: List<Record<IData>> = recordCache.values
            .flatMap { it.values }
            .map { it as Record<IData> }

        AutoSaveExecutor.batchRecordChange(recordList)
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : IData> addCache(clazz: Class<V>, record: Record<V>) {
        val map = recordCache.getOrPut(clazz) { mutableMapOf() }
        (map as MutableMap<Long, Record<V>>)[record.id] = record
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : IData> getCache(clazz: Class<V>, id: Long): V? {
        val map = recordCache[clazz] ?: return null
        return (map[id] as? Record<V>)?.value
    }

}