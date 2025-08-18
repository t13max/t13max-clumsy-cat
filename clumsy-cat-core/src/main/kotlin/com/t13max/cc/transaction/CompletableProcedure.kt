package com.t13max.cc.transaction

import kotlinx.coroutines.CompletableDeferred


/**
 * 带结果的存储过程
 *
 * @author t13max
 * @since 17:23 2025/7/10
 */
abstract class CompletableProcedure<T> : Procedure() {

    //completable
    private val completable = CompletableDeferred<T>()

    //同步等待结果
    suspend fun sync(): T {
        return completable.await()
    }

    //完成
    protected fun complete(result: T) {
        completable.complete(result)
    }

}

