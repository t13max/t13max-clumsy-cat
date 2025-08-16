package com.t13max.cc.transaction

import com.t13max.cc.utils.Log
import kotlinx.coroutines.Job

/**
 * 存储过程
 *
 * @author t13max
 * @since 17:23 2025/7/10
 */
abstract class Procedure {

    private var success = false

    /**
     * call方法 执行逻辑带返回值
     * 会被Transaction调用 或者已经在 Procedure.process内 直接在当前事务执行
     *
     * @Author t13max
     * @Date 13:40 2025/8/16
     */
    suspend fun call(): Boolean {
        //当前是否在事务内 不在则创建
        if (Transaction.current() == null) {

            try {
                Transaction.execute(this)
            } catch (throwable: Throwable) {
                //创建失败???
                Log.TRANSACT.error("Transaction create error!", throwable)
            }
        }

        try {
            if (process()) {
                commit()
                success = true
                return true
            }
        } catch (throwable: Throwable) {
            Log.TRANSACT.error("Procedure execute error! ", throwable)
        }

        rollback()

        return false
    }

    /**
     * 回滚事务
     *
     * @Author t13max
     * @Date 13:40 2025/8/16
     */
    private suspend fun rollback() {

    }

    /**
     * 提交事务
     *
     * @Author t13max
     * @Date 13:40 2025/8/16
     */
    private suspend fun commit() {

    }

    /**
     * 提交一个事务 有返回值
     * 返回值可以优化一下
     * @Author t13max
     * @Date 13:39 2025/8/16
     */
    fun submit(): Job {
        return Transaction.submit(this)
    }

    /**
     * 执行一个事务 无返回值
     *
     * @Author t13max
     * @Date 13:39 2025/8/16
     */
    fun execute() {
        Transaction.execute(this)
    }

    private suspend fun verify(): Boolean {
        var current = Transaction.current()
        if (current == null) {
            Log.TRANSACT.error("verify, Transaction is null!")
            return false
        }
        return true
    }

    /**
     * 执行逻辑, 写业务代码的地方
     *
     * @Author t13max
     * @Date 13:38 2025/8/16
     */
    protected open suspend fun process(): Boolean {
        return false
    }

    /**
     * commit后执行任务
     *
     * @Author t13max
     * @Date 13:38 2025/8/16
     */
    protected suspend fun executeWhileCommit(task: suspend () -> Unit) {
        var current = Transaction.current()
        if (current == null) {
            Log.TRANSACT.error("executeWhileCommit, Transaction is null!")
            return
        }
        current.executeWhileCommit(task)
    }

    /**
     * rollback后执行任务
     *
     * @Author t13max
     * @Date 13:38 2025/8/16
     */
    protected suspend fun executeWhileRollback(task: suspend () -> Unit) {
        var current = Transaction.current()
        if (current == null) {
            Log.TRANSACT.error("executeWhileRollback, Transaction is null!")
            return
        }
        current.executeWhileCommit(task)
    }

}

