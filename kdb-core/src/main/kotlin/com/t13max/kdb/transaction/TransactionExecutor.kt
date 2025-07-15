package com.t13max.kdb.transaction

import com.t13max.kdb.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 事务执行器
 *
 * @author t13max
 * @since 11:23 2025/7/12
 */
class TransactionExecutor {

    companion object {

        //事务集合 总感觉不太好
        private val transactionMap = ConcurrentHashMap<Job, Transaction>()

        /**
         * 提交一个事务
         *
         * @Author t13max
         * @Date 11:30 2025/7/12
         */
        fun submit(procedure: Procedure): Job {

            //新起一个协程执行
            val job = Utils.transactionScope.launch {
                Transaction.create().perform(procedure)
            }

            return job
        }

        fun execute(procedure: Procedure) {

            submit(procedure)
        }

        fun putTransaction(job: Job, transaction: Transaction) {
            transactionMap.put(job, transaction)
        }

        fun removeTransaction(job: Job?): Transaction? {
            if (job == null) {
                return null
            }
            return transactionMap.remove(job)
        }

        fun getTransaction(job: Job): Transaction? {
            return transactionMap.get(job)
        }

    }

    fun shutdown() {
        Utils.transactionScope.coroutineContext[Job]?.cancel()
    }
}