package com.t13max.cc.transaction

import com.t13max.cc.utils.Log
import kotlinx.coroutines.CompletableDeferred

/**
 * 分布式存储过程
 * TCC模式
 *
 * @Author: t13max
 * @Since: 5:57 2025/8/17
 */
abstract class DistributedProcedure : Procedure() {

    //try成功信号
    private val trySuccess = CompletableDeferred<Boolean>()

    //提交信号
    private val commitSignal = CompletableDeferred<Boolean>()

    //提交成功信号
    private val commitSuccess = CompletableDeferred<Boolean>()

    override suspend fun process(): Boolean {


        try {
            //try
            if (!tryProcess()) {
                this.trySuccess.complete(false)
                return false
            }
        } catch (throwable: Throwable) {
            Log.TRANSACT.error("try异常, ", throwable)
            this.trySuccess.complete(false)
            return false
        }

        this.trySuccess.complete(true)

        //成功了 等待提交
        val await = commitSignal.await()

        //结果是false 也就是取消
        if (!await) {
            try {
                //尝试取消
                if (cancel()) {
                    return false
                }
            } catch (throwable: Throwable) {
                Log.TRANSACT.error("cancel异常, ", throwable)
                //增加重试逻辑...
            }
            return false
        }

        try {
            //尝试提交
            if (commit()) {
                this.commitSuccess.complete(true)
                return true
            }
        } catch (throwable: Throwable) {
            Log.TRANSACT.error("commit异常, ", throwable)
        }
        this.commitSuccess.complete(false)
        return false
    }

    //try
    protected abstract suspend fun tryProcess(): Boolean

    //提交
    open protected suspend fun commit(): Boolean {
        return true
    }

    //取消
    open protected suspend fun cancel(): Boolean {
        return true
    }

    /**
     * 通知可以提交或者需要回滚
     *
     * @Author: t13max
     * @Since: 6:07 2025/8/17
     */
    fun notifySignal(commit: Boolean) {
        commitSignal.complete(commit)
    }

    /**
     * try成功的信号句柄
     * 提交事务后 需要判断是否成功了
     *
     * @Author: t13max
     * @Since: 6:59 2025/8/17
     */
    fun trySuccessDeferred(): CompletableDeferred<Boolean> {
        return trySuccess
    }

    /**
     * commit成功的信号句柄
     * 事务调校后 需要判断是否成功了
     *
     * @Author: t13max
     * @Since: 7:00 2025/8/17
     */
    fun commitSuccessDeferred(): CompletableDeferred<Boolean> {
        return commitSuccess
    }
}