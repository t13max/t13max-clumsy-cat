package com.t13max.kdb.transaction

import com.t13max.kdb.bean.IData
import com.t13max.kdb.lock.LockCache
import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.utils.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * 事务
 *
 * @author t13max
 * @since 16:50 2025/7/9
 */
class Transaction(private val transactionId: Long) {

    companion object {

        //协程事务集合
        private val transactionMap = ConcurrentHashMap<Long, Transaction>()

        private suspend fun coroutineId(): Long {
            return coroutineContext[TransactionIdKey]?.id
                ?: throw IllegalStateException("Coroutine does not have a TransactionId")
        }

        suspend fun current(): Transaction? {
            return transactionMap[coroutineId()]
        }

        suspend fun create(transactionId: Long): Transaction {
            val tx = Transaction(transactionId)
            transactionMap[transactionId] = tx
            return tx
        }

        suspend fun destroy() {
            transactionMap.remove(coroutineId())
        }

        // 每次事务 一个id
        object TransactionIdKey : CoroutineContext.Key<TransactionIdElement>
        class TransactionIdElement(val id: Long) : AbstractCoroutineContextElement(TransactionIdKey)
    }

    //事务缓存
    private val transactionCache = mutableMapOf<Class<out IData>, MutableMap<Long, IData>>()

    //当前事务持有的锁
    private val lockCache = mutableListOf<RecordLock>()

    //提交后执行任务
    private val whileCommitTaskList = mutableListOf<Runnable>()

    //回滚后执行的任务
    private val whileRollbackTaskList = mutableListOf<Runnable>()

    //添加持有的锁
    suspend fun addLock(lock: RecordLock) {
        this.lockCache.add(lock)
    }

    /**
     * 执行Procedure
     *
     * @Author t13max
     * @Date 11:02 2025/7/12
     */
    suspend fun perform(procedure: Procedure) {

        val job = coroutineContext[Job]

        try {

            //flush锁
            LockCache.flushLock().readLock(job)

            //执行call
            if (procedure.call()) {
                //成功 提交后逻辑
                commitAfter()
            } else {
                //失败了 回滚后逻辑
                rollbackAfter()
            }
        } catch (throwable: Throwable) {
            //异常了 回滚
            Log.TRANSACT.error("perform exception!", throwable)
            rollbackAfter()
        } finally {
            //finally
            finish(procedure)
            //释放读锁
            LockCache.flushLock().readUnlock(job)
            //销毁
            destroy()
        }
    }

    /**
     * Procedure调用的 回滚
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun rollback() {
        Log.TRANSACT.info("rollback!  transactionId={}", transactionId)
        for (valueMap in transactionCache.values) {
            for (data in valueMap.values) {
                data.rollback()
            }
        }
    }

    /**
     * Procedure调用的 提交
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun commit() {
        Log.TRANSACT.info("commit!  transactionId={}", transactionId)
        for (valueMap in transactionCache.values) {
            for (data in valueMap.values) {
                data.commit()
            }
        }
    }

    /**
     * Transaction自己调用的 最后的提交
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun commitAfter() {
        for (runnable in whileCommitTaskList) {
            runTask(runnable)
        }
    }

    /**
     * Transaction自己调用的 最后的回滚
     *
     * @Author t13max
     * @Date 11:01 2025/7/12
     */
    suspend fun rollbackAfter() {
        for (runnable in whileRollbackTaskList) {
            runTask(runnable)
        }
    }

    /**
     * 执行任务
     *
     * @Author t13max
     * @Date 13:43 2025/8/16
     */
    fun runTask(task: Runnable) {
        try {
            task.run()
        } catch (exception: Exception) {
            //异常处理
            Log.TRANSACT.error("runTask error! transactionId={}, task={}", transactionId, task)
        }
    }

    /**
     * 提交后执行
     *
     * @Author t13max
     * @Date 13:44 2025/8/16
     */
    fun executeWhileCommit(task: Runnable) {
        whileCommitTaskList.add(task)
    }

    /**
     * 回滚后执行
     *
     * @Author t13max
     * @Date 13:44 2025/8/16
     */
    fun executeWhileRollback(task: Runnable) {
        whileRollbackTaskList.add(task)
    }

    /**
     * 执行完 最终调用
     *
     * @Author t13max
     * @Date 11:03 2025/7/12
     */
    private suspend fun finish(procedure: Procedure) {

        //移除事务
        TransactionExecutor.removeTransaction(transactionId)

        //释放锁
        for (lock in lockCache) {
            lock.unlock()
        }

        //异步存库 如果有的话...

        /*val recordList: List<Record<IData>> = recordCache.values
            .flatMap { it.values }
            .map { it as Record<IData> }

        AutoSaveExecutor.batchRecordChange(recordList)*/
    }

    /**
     * 添加到事务缓存
     *
     * @Author t13max
     * @Date 13:44 2025/8/16
     */
    fun <V : IData> addCache(clazz: Class<V>, value: V) {
        val cacheMap = transactionCache.getOrPut(clazz) { mutableMapOf<Long, IData>() }
        cacheMap[value.id] = value
    }

    /**
     * 从事务缓存获取数据
     *
     * @Author t13max
     * @Date 13:44 2025/8/16
     */
    fun <V : IData> getCache(clazz: Class<V>, id: Long): V? {
        val map = transactionCache[clazz] ?: return null
        @Suppress("UNCHECKED_CAST")
        return map[id] as? V
    }

}