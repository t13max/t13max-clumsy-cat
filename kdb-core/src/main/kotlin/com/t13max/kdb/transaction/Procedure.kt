package com.t13max.kdb.transaction

import com.t13max.kdb.transaction.Transaction.Companion.transactionLocal
import com.t13max.kdb.utils.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author t13max
 * @since 17:23 2025/7/10
 */
open class Procedure {

    protected var coroutineContext: CoroutineContext? = null

    private var success = false

    suspend fun call(): Boolean {

        if (Transaction.current() == null) {

            try {
                // perform 将回调本函数,然后执行事务已经存在的分支。
                Transaction.create().perform(this)
            } catch (throwable: Throwable) {
                Log.TRANSACT.error("Transaction create error!${throwable.message}")
            } finally {
                Transaction.destroy()
                this.fetchTasks()
            }
        }

        coroutineContext = currentCoroutineContext()

        try {
            if (process()) {
                commit()
                success = true
                return true
            }
        } catch (throwable: Throwable) {
            Log.TRANSACT.error("Procedure execute error! ${throwable.message}")
        }

        rollback()

        return false
    }

    suspend fun fetchTasks() {

    }

    suspend fun rollback() {
        Transaction.current().rollback()
    }

    suspend fun commit() {
        Transaction.current().commit()
    }

    fun submit() {
        TransactionExecutor.submit(this)
    }

    fun execute() {
        TransactionExecutor.execute(this)
    }

    private suspend fun verify() {
        var current = Transaction.current()

    }

    @Throws(Exception::class)
    protected open fun process(): Boolean {
        return false
    }

    fun getJob(): Job? {
        return coroutineContext?.get(Job)
    }

    fun currentTransaction(): Transaction {
        return transactionLocal.get(context)
    }
}

