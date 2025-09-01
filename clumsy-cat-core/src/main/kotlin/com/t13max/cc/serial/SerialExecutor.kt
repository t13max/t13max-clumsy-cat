package com.t13max.cc.serial

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.collections.getOrPut
import kotlin.coroutines.cancellation.CancellationException

//串行任务 带结果
data class SerialTaskWithResult<T>(
    val isIO: Boolean,
    val action: suspend () -> T,
    val result: CompletableDeferred<T>
)

// Channel包装类，包含状态信息和关闭逻辑
private class ChannelWrapper<E : Enum<E>>(
    val type: E,
    val id: Long,
    val channel: Channel<SerialTaskWithResult<*>>,
    var lastUsedTime: Long = System.currentTimeMillis(),
    var isClosed: Boolean = false,
    var processingJob: Job? = null
) {

    val mutex = Mutex() // wrapper级别锁

    fun updateLastUsedTime() {
        lastUsedTime = System.currentTimeMillis()
    }
}

/**
 * 串行执行器
 * 多个作用域 多个Channel
 *
 * @author cxcm
 * @Date 13:53 2025/6/26
 */
class SerialExecutor<E : Enum<E>>() {

    //logger
    companion object {
        //日志
        private val logger = LoggerFactory.getLogger(SerialExecutor::class.java)

        // 创建基于虚拟线程的 CoroutineDispatcher
        private val virtualThreadDispatcher: CoroutineDispatcher =
            Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

        private const val IDLE_TIMEOUT_MILLIS = 60000L  // 空闲关闭间隔，60秒
        private const val ACTION_TIMEOUT_MILLIS = 30000L // action超时时间，30秒
        private const val CLEANUP_INTERVAL_MILLIS = 10000L // 清理间隔，10秒
        private const val MAX_RETRY_COUNT = 3 // 清理间隔，10秒
    }

    // Channel包装集合
    private val channelWrappers = mutableMapOf<E, MutableMap<Long, ChannelWrapper<E>>>()

    // 避免并发创建和清理channel
    private val mutex = Mutex()

    // 清理任务
    private val cleanupJob: Job

    //scope
    private val sharedScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        // 启动定期清理任务
        cleanupJob = sharedScope.launch {
            while (true) {
                delay(CLEANUP_INTERVAL_MILLIS)
                cleanupIdleChannels()
            }
        }
    }

    /**
     * 提供给kotlin的等待结果的
     */
    suspend fun <T> submitWithResult(type: E, id: Long, isIO: Boolean = false, action: suspend () -> T): T {
        val result = CompletableDeferred<T>()
        submitTask(type, id, isIO, action, result)
        return result.await()
    }

    /**
     * 提交一个任务 返回CompletableDeferred
     */
    suspend fun <T> submit(type: E, id: Long, isIO: Boolean = false, action: suspend () -> T): CompletableDeferred<T> {
        val result = CompletableDeferred<T>()
        submitTask(type, id, isIO, action, result)
        return result
    }

    /**
     * 提供给java的 返回Future的方法
     */
    fun <T> submitWithFuture(type: E, id: Long, isIO: Boolean = false, callable: Callable<T>): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        sharedScope.launch {
            try {
                val result = submitWithResult(type, id, isIO) { callable.call() }
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    /**
     * 提交任务到对应的Channel
     */
    private suspend fun <T> submitTask(type: E, id: Long, isIO: Boolean, action: suspend () -> T, result: CompletableDeferred<T>) {

        var retryCount = 0

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                val wrapper = getOrCreateChannelWrapper(type, id)

                // wrapper级别锁，保证提交任务和关闭互斥
                wrapper.mutex.lock()
                try {
                    if (wrapper.isClosed) {
                        logger.debug("Channel is closed, recreating: type={}, id={}", type, id)
                        removeClosedWrapper(type, id)
                        retryCount++
                        continue
                    }

                    wrapper.updateLastUsedTime()
                    @Suppress("UNCHECKED_CAST")
                    wrapper.channel.send(SerialTaskWithResult(isIO, action, result) as SerialTaskWithResult<*>)
                    return
                } finally {
                    wrapper.mutex.unlock()
                }
            } catch (e: Exception) {
                if (e is IllegalStateException) {
                    logger.debug("Send failed due to closed channel, recreating: type={}, id={}, attempt={}", type, id, retryCount + 1)
                    removeClosedWrapper(type, id)
                    retryCount++
                    if (retryCount >= MAX_RETRY_COUNT) {
                        result.completeExceptionally(IllegalStateException("Failed to send task after $MAX_RETRY_COUNT attempts for type=$type, id=$id"))
                        return
                    }
                    delay(10)
                } else {
                    result.completeExceptionally(e)
                    return
                }
            }
        }
    }

    /**
     * 移除已关闭的wrapper
     */
    private suspend fun removeClosedWrapper(type: E, id: Long) {
        mutex.lock()
        try {
            channelWrappers[type]?.remove(id)
        } finally {
            mutex.unlock()
        }
    }

    /**
     * 获取Channel包装，不存在则创建
     */
    private suspend fun getOrCreateChannelWrapper(type: E, id: Long): ChannelWrapper<E> {
        // 先尝试获取现有的wrapper（不加锁，快速路径）
        val existingWrapper = channelWrappers[type]?.get(id)
        if (existingWrapper != null && !existingWrapper.isClosed) {
            return existingWrapper
        }

        mutex.lock()
        try {
            val map = channelWrappers.getOrPut(type) { mutableMapOf() }

            // 再次检查，避免在获取锁期间已经被其他线程创建
            val existingWrapperWithLock = map[id]
            if (existingWrapperWithLock != null && !existingWrapperWithLock.isClosed) {
                return existingWrapperWithLock
            }

            // 创建新的Channel和wrapper
            val channel = Channel<SerialTaskWithResult<*>>(Channel.UNLIMITED)
            val wrapper = ChannelWrapper(type, id, channel)

            // 启动Channel处理协程
            val processingJob = sharedScope.launch {
                processChannelTasks(wrapper)
            }
            wrapper.processingJob = processingJob

            map[id] = wrapper
            logger.debug("Created new channel: type={}, id={}", type, id)
            return wrapper
        } finally {
            mutex.unlock()
        }
    }

    /**
     * 处理Channel中的任务
     */
    private suspend fun processChannelTasks(wrapper: ChannelWrapper<E>) {
        val channel = wrapper.channel

        while (!wrapper.isClosed) {
            try {
                val task = channel.receive()
                wrapper.updateLastUsedTime()

                try {
                    // 带超时的任务执行
                    val value = withTimeout(ACTION_TIMEOUT_MILLIS) {
                        if (task.isIO) {
                            withContext(virtualThreadDispatcher) { task.action() }
                        } else {
                            task.action()
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    (task.result as CompletableDeferred<Any?>).complete(value)
                } catch (e: Exception) {
                    when (e) {
                        is kotlinx.coroutines.TimeoutCancellationException -> {
                            logger.warn("Action timeout! type=${wrapper.type}, id=${wrapper.id}")
                            task.result.completeExceptionally(java.util.concurrent.TimeoutException("Action timeout after $ACTION_TIMEOUT_MILLIS ms"))
                        }

                        /*!is StatusException -> {
                            logger.error("Action failed! type=${wrapper.type}, id=${wrapper.id}, message=${e.message}", e)
                            task.result.completeExceptionally(e)
                        }*/

                        else -> {
                            task.result.completeExceptionally(e)
                        }
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is kotlinx.coroutines.channels.ClosedReceiveChannelException -> {
                        // 正常关闭
                        break
                    }

                    is CancellationException -> {
                        // 被 cancel 了 也是正常的
                        logger.debug("Processing job cancelled for type={}, id={}", wrapper.type, wrapper.id)
                        break
                    }

                    else -> {
                        logger.error("Error processing task in channel type=${wrapper.type}, id=${wrapper.id}", e)
                    }
                }
            }
        }

        logger.debug("Channel processing stopped for type={}, id={}", wrapper.type, wrapper.id)
    }

    /**
     * 清理空闲的Channel
     */
    private suspend fun cleanupIdleChannels() {
        mutex.lock()
        try {
            val now = System.currentTimeMillis()
            val wrappersToClose = mutableListOf<ChannelWrapper<E>>()

            // 收集需要关闭的wrapper
            for ((type, idMap) in channelWrappers) {
                for ((id, wrapper) in idMap) {
                    wrapper.mutex.lock()
                    try {
                        if (!wrapper.isClosed && now - wrapper.lastUsedTime > IDLE_TIMEOUT_MILLIS) {
                            wrapper.isClosed = true
                            // 标记为正在关闭，禁止新任务提交
                            wrappersToClose.add(wrapper)
                        }
                    } finally {
                        wrapper.mutex.unlock()
                    }
                }
            }

            // 关闭收集到的wrapper
            for (wrapper in wrappersToClose) {
                wrapper.mutex.lock()
                try {
                    closeChannelWrapper(wrapper)
                    channelWrappers[wrapper.type]?.remove(wrapper.id)
                } finally {
                    wrapper.mutex.unlock()
                }
            }
        } finally {
            mutex.unlock()
        }
    }

    /**
     * 关闭Channel包装
     */
    private fun closeChannelWrapper(wrapper: ChannelWrapper<E>) {
        wrapper.isClosed = true
        wrapper.processingJob?.cancel()
        wrapper.channel.close()
        logger.debug("Channel closed: type={}, id={}", wrapper.type, wrapper.id)
    }

    /**
     * 关闭所有Channel并停止清理任务
     */
    suspend fun shutdown() {
        cleanupJob.cancel()

        mutex.lock()
        try {
            for ((_, idMap) in channelWrappers) {
                for (wrapper in idMap.values) {
                    closeChannelWrapper(wrapper)
                }
            }
            channelWrappers.clear()
        } finally {
            mutex.unlock()
        }
    }

    /**
     * 获取当前活跃的Channel数量（用于监控）
     */
    suspend fun getActiveChannelCount(): Int {
        mutex.lock()
        try {
            return channelWrappers.values.sumOf { it.size }
        } finally {
            mutex.unlock()
        }
    }

    /**
     * 强制关闭指定类型的Channel（用于测试或特殊情况）
     */
    suspend fun forceCloseChannel(type: E, id: Long) {
        mutex.lock()
        try {
            val wrapper = channelWrappers[type]?.get(id)
            if (wrapper != null && !wrapper.isClosed) {
                closeChannelWrapper(wrapper)
                channelWrappers[type]?.remove(id)
            }
        } finally {
            mutex.unlock()
        }
    }

}