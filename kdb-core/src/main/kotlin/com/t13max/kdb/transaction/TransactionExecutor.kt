package com.t13max.kdb.transaction

import com.t13max.kdb.utils.Utils
import com.t13max.kdb.utils.UuidUtils
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
        private val transactionMap = ConcurrentHashMap<Long, Transaction>()

        /**
         * 提交一个事务
         *
         * @Author t13max
         * @Date 11:30 2025/7/12
         */
        fun submit(procedure: Procedure): Job {

            val transactionId = UuidUtils.getNextUuid()

            return Utils.transactionScope.launch(Transaction.Companion.TransactionIdElement(transactionId)) {
                Transaction.create(transactionId).perform(procedure)
            }
        }

        fun execute(procedure: Procedure) {

            submit(procedure)
        }

        fun putTransaction(id: Long, transaction: Transaction) {
            transactionMap.put(id, transaction)
        }

        fun removeTransaction(id: Long): Transaction? {
            return transactionMap.remove(id)
        }

        fun getTransaction(id: Long): Transaction? {
            return transactionMap.get(id)
        }

    }

    fun shutdown() {
        Utils.transactionScope.coroutineContext[Job]?.cancel()
    }
}