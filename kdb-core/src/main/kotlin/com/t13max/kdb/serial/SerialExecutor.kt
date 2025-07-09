package com.t13max.kdb.serial

import com.t13max.kdb.utils.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 串行执行器
 * 多个作用域 多个Channel
 *
 * @Author t13max
 * @Date 13:53 2025/6/26
 */
class SerialExecutor<E : Enum<E>>() {

    //channel集合 长时间不用需要手动关闭掉
    private val actionChannels = mutableMapOf<E, MutableMap<Long, Channel<suspend () -> Unit>>>()

    // 避免并发创建channel
    private val mutex = Mutex()

    //scope
    private val sharedScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 提供给kotlin的提交任务的方法
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    suspend fun submit(type: E, id: Long, action: suspend () -> Unit) {
        getOrCreateChannel(type, id).send(action)
    }

    /**
     * 提供给java的提交任务的方法
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    fun submit(type: E, id: Long, runnable: Runnable) {
        sharedScope.launch {
            getOrCreateChannel(type, id).send {
                runnable.run()
            }
        }
    }

    /**
     * 获取channel 不存在则创建
     *
     * @Author t13max
     * @Date 13:53 2025/6/26
     */
    private suspend fun getOrCreateChannel(type: E, id: Long): Channel<suspend () -> Unit> {

        // 无锁快速返回
        actionChannels[type]?.get(id)?.let { return it }

        mutex.lock()

        try {

            //根据type取
            val map = actionChannels.getOrPut(type) { mutableMapOf() }

            //再根据id拿
            return map.getOrPut(id) {
                //新建无界channel
                val channel = Channel<suspend () -> Unit>(Channel.UNLIMITED)
                //启动一个协程 从这个channel里不断拿到任务区执行
                sharedScope.launch {

                    //这里的for会不断地拿
                    for (action in channel) {
                        try {

                            //暂时写死5秒超时
                            withTimeoutOrNull(5000) {
                                //执行
                                action()
                            } ?: Log.SERIAL.warn("action timeout! type=$type, id=$id")
                        } catch (e: Exception) {
                            //记录异常
                            Log.SERIAL.error("action failed! type=$type, id=$id, message=${e.message}", e)
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