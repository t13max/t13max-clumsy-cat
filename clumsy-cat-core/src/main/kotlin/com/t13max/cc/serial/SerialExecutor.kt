package com.t13max.cc.serial

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.getOrPut
import kotlin.jvm.java
import kotlin.let
import kotlin.run

/**
 * 串行执行器
 * 多个作用域 多个Channel
 *
 * @author cxcm
 * @Date 13:53 2025/6/26
 */

data class SerialTaskWithResult<T>(
    val isIO: Boolean,
    val action: suspend () -> T,
    val result: CompletableDeferred<T>
)

class SerialExecutor<E : Enum<E>>() {

    //logger
    companion object {

        //日志
        private val logger = LoggerFactory.getLogger(SerialExecutor::class.java)

        // 创建基于虚拟线程的 CoroutineDispatcher
        private val virtualThreadDispatcher: CoroutineDispatcher =
            Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

        private const val IDLE_TIMEOUT_MILLIS = 60000L  // 60秒无任务自动关闭

        private const val ACTION_TIMEOUT_MILLS = Long.MAX_VALUE; //action超时时间
    }

    //channel集合 长时间不用需要手动关闭掉
    private val actionChannels = mutableMapOf<E, MutableMap<Long, Channel<SerialTaskWithResult<*>>>>()

    // 避免并发创建channel
    private val mutex = Mutex()

    //scope
    private val sharedScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 提供给kotlin的提交任务的方法
     *
     * @author cxcm
     * @Date 13:53 2025/6/26
     */
    suspend fun <T> submitWithResult(type: E, id: Long, isIO: Boolean = false, action: suspend () -> T): T {
        val result = CompletableDeferred<T>()
        getOrCreateChannel(type, id).send(SerialTaskWithResult(isIO, action, result))
        return result.await()
    }

    /**
     * 提供给java的提交任务的方法
     *
     * @author cxcm
     * @Date 13:53 2025/6/26
     */
    fun <T> submitWithResultJ(type: E, id: Long, isIO: Boolean = false, callable: Callable<T>): T {
        val result = CompletableDeferred<T>()
        sharedScope.launch {
            getOrCreateChannel(type, id).send(SerialTaskWithResult(isIO, { callable.call() }, result))
        }
        // 阻塞等待（给 Java 用）
        return runBlocking { result.await() }
    }

    /**
     * 获取channel 不存在则创建
     *
     * @author cxcm
     * @Date 13:53 2025/6/26
     */
    private suspend fun getOrCreateChannel(type: E, id: Long): Channel<SerialTaskWithResult<*>> {
        actionChannels[type]?.get(id)?.let { return it }
        mutex.lock()
        try {
            val map = actionChannels.getOrPut(type) { mutableMapOf() }
            return map.getOrPut(id) {
                val channel = Channel<SerialTaskWithResult<*>>(Channel.UNLIMITED)
                sharedScope.launch {
                    while (true) {
                        val task = withTimeoutOrNull(IDLE_TIMEOUT_MILLIS) { channel.receive() }
                            ?: run {
                                logger.debug("channel idle timeout type={} id={} remove", type, id)
                                mutex.withLock {
                                    actionChannels[type]?.remove(id)
                                    if (actionChannels[type]?.isEmpty() == true) {
                                        actionChannels.remove(type)
                                    }
                                }
                                logger.debug("channel idle timeout type={} id={} close", type, id)
                                channel.close()
                                return@launch
                            }
                        try {
                            val value = withTimeoutOrNull(ACTION_TIMEOUT_MILLS) {
                                if (task.isIO) {
                                    withContext(virtualThreadDispatcher) {
                                        task.action()
                                    }
                                } else {
                                    task.action()
                                }
                            } ?: run {
                                logger.warn("action timeout! type=$type, id=$id")
                                null
                            }
                            @Suppress("UNCHECKED_CAST")
                            (task.result as CompletableDeferred<Any?>).complete(value)
                        } catch (e: Exception) {
                            //logger.error("action failed! type=$type, id=$id, message=${e.message}", e)
                            task.result.completeExceptionally(e)
                        }
                    }
                }
                channel
            }
        } finally {
            mutex.unlock()
        }
    }
}