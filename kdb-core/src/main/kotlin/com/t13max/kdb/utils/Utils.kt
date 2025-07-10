package com.t13max.kdb.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 *
 * @author t13max
 * @since 16:04 2025/7/10
 */
class Utils {

    companion object {

        // 创建基于虚拟线程的 CoroutineDispatcher
        val virtualThreadDispatcher: CoroutineDispatcher = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

        //穿行
        val serialScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        //事务
        val transactionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        //自动存库
        val autoSaveScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    }
}