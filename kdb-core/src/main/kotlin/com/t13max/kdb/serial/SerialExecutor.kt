package com.t13max.kdb.serial

import com.t13max.kdb.lock.LockCache
import com.t13max.kdb.utils.Log
import com.t13max.kdb.utils.Utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

/**
 * 串行执行器
 * 多个作用域 多个Channel
 *
 * @Author t13max
 * @Date 13:53 2025/6/26
 */

class SerialExecutor() {

    //channel集合 长时间不用需要手动关闭掉
    private val actionChannels = mutableMapOf<String, MutableMap<Long, ChannelWrapper>>()

    // 避免并发创建channel
    private val mutex = Mutex()

    init {

        Utils.serialScope.launch {
            while (isActive) {
                delay(60_000) // 每分钟检查一次
                val now = System.currentTimeMillis()
                mutex.lock()
                try {
                    actionChannels.forEach { (scope, map) ->
                        val idsToRemove = mutableListOf<Long>()
                        map.forEach { (id, wrapper) ->
                            if (!wrapper.channel.isClosedForSend && now - wrapper.lastActiveTime > 5 * 60 * 1000) {
                                wrapper.channel.close()
                                idsToRemove.add(id)
                            }
                        }
                        idsToRemove.forEach { map.remove(it) }
                        if (map.isEmpty()) actionChannels.remove(scope)
                    }
                } finally {
                    mutex.unlock()
                }
            }
        }
    }

    /**
     * 提供给kotlin的提交任务的方法
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    suspend fun submit(serialTask: SerialTask) {
        getOrCreateChannel(serialTask.scope, serialTask.id).channel.send(serialTask)
    }

    /**
     * 提供给java的提交任务的方法
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    fun submit4j(serialTask: SerialTask) {
        Utils.serialScope.launch {
            submit(serialTask)
        }
    }

    /**
     * 获取channel 不存在则创建
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    private suspend fun getOrCreateChannel(scope: String, id: Long): ChannelWrapper {

        // 无锁快速返回
        actionChannels[scope]?.get(id)?.let { return it }

        mutex.lock()

        try {

            //根据type取
            val map = actionChannels.getOrPut(scope) { mutableMapOf() }

            //再根据id拿
            return map.getOrPut(id) {

                //新建无界channel
                val channel = Channel<SerialTask>(Channel.UNLIMITED)

                val channelWrapper = ChannelWrapper(channel, System.currentTimeMillis())
                //启动一个协程 从这个channel里不断拿到任务区执行
                Utils.serialScope.launch {

                    //这里的for会不断地拿
                    for (task in channel) {

                        channelWrapper.lastActiveTime = System.currentTimeMillis()

                        //拿到scope锁
                        val lock = LockCache.getLock(task.scope, task.id)

                        //锁住 这里加锁是为了防止与事务那边同时执行
                        lock.lock()

                        try {

                            withTimeoutOrNull(5000) {
                                if (task.io) {
                                    //在IO线程执行 挂起等待
                                    withContext(Utils.virtualThreadDispatcher) {
                                        task.run()
                                    }
                                } else {
                                    //直接执行
                                    task.run()
                                }
                            } ?: Log.SERIAL.warn("action timeout! scope=$scope, id=$id")
                        } catch (e: Exception) {
                            //记录异常
                            Log.SERIAL.error("action failed! scope=$scope, id=$id, message=${e.message}", e)
                        } finally {
                            lock.unlock()
                        }
                    }
                }
                channelWrapper
            }
        } finally {
            mutex.unlock()
        }
    }

    fun shutdown() {
        Utils.serialScope.coroutineContext[Job]?.cancel()
    }

}


private data class ChannelWrapper(
    val channel: Channel<SerialTask>,
    var lastActiveTime: Long
)