package com.t13max.cc.executor

import com.t13max.cc.KdbEngine
import com.t13max.cc.bean.IData
import com.t13max.cc.bean.Option
import com.t13max.cc.lock.LockCache
import com.t13max.cc.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 自动存库执行器
 * 待完善
 *
 * @author t13max
 * @since 18:58 2025/7/9
 */
class AutoSaveExecutor() {


    companion object {

        //单例 懒加载?
        private var INSTANCE: AutoSaveExecutor = AutoSaveExecutor()

        fun inst(): AutoSaveExecutor {
            return INSTANCE
        }
    }

    private val changeChannel: Channel<IData> = Channel(Channel.Factory.UNLIMITED)

    private val saveMap = mutableMapOf<Option, IData>()

    fun <V : IData> recordChange(value: V) {
        if (!KdbEngine.inst().conf.auto.isOpen) {
            return
        }
        //提交到channel
        Utils.Companion.autoSaveScope.launch {
            changeChannel.send(value)
        }
    }

    fun <V : IData> batchRecordChange(recordList: List<V>) {
        if (!KdbEngine.inst().conf.auto.isOpen) {
            return
        }
        //提交到channel
        Utils.Companion.autoSaveScope.launch {
            for (record in recordList) {
                changeChannel.send(record)
            }
        }
    }

    suspend fun <T> Channel<T>.receiveBatch(maxBatchSize: Int, timeoutMs: Long): List<T> {
        val list = mutableListOf<T>()
        val start = System.currentTimeMillis()
        while (list.size < maxBatchSize && System.currentTimeMillis() - start < timeoutMs) {
            val item = withTimeoutOrNull(timeoutMs - (System.currentTimeMillis() - start)) {
                receive()
            } ?: break
            list.add(item)
        }
        return list
    }

    suspend fun transferN() {

        //深拷贝对象 转移数据
    }

    suspend fun transfer0() {

        val flushLock = LockCache.flushLock()

        flushLock.writeLock()

        try {
            //深拷贝对象 转移数据

        } finally {
            flushLock.writeUnlock()
        }
    }

    suspend fun save() {
        //调用数据层 保存数据
    }

    fun start() {
        Utils.Companion.autoSaveScope.launch {

            // 协程取消时自动退出
            while (isActive) {

                //从channel拷贝转移数据到saveMap
                transferN()

                //加锁 保证一个完整的检查点
                transfer0()

                //存库
                save()

                delay(60_000) // 每秒执行一次
            }
        }
    }

    /**
     * 优雅关闭 应该执行完左右任务
     *
     * @Author t13max
     * @Date 17:55 2025/8/16
     */
    fun shutdown() {
        Utils.autoSaveScope.coroutineContext[Job]?.cancel()
    }
}