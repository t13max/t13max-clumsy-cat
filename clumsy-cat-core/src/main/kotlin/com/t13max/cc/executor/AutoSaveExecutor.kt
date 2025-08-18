package com.t13max.cc.executor

import com.t13max.cc.ClumsyCatEngine
import com.t13max.cc.bean.AutoData
import com.t13max.cc.bean.DataOption
import com.t13max.cc.bean.Option
import com.t13max.cc.lock.LockCache
import com.t13max.cc.utils.DeepCopyUtil
import com.t13max.cc.utils.Log
import com.t13max.cc.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 自动存库执行器
 * 失败重试逻辑待完善
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

    private val changeChannel: Channel<AutoData> = Channel(Channel.Factory.UNLIMITED)

    //插入集合
    private val insertMap = mutableMapOf<Class<out AutoData>, MutableList<AutoData>>()

    //删除集合
    private val deleteMap = mutableMapOf<Class<out AutoData>, MutableList<AutoData>>()

    //更新集合
    private val updateMap = mutableMapOf<Class<out AutoData>, MutableList<AutoData>>()

    //拷贝集合
    private val copyDataMap = mutableMapOf<AutoData, AutoData>()

    fun <V : AutoData> valueChange(value: V) {
        if (!ClumsyCatEngine.inst().conf.auto.isOpen) {
            return
        }
        //提交到channel
        Utils.autoSaveScope.launch {
            changeChannel.send(value)
        }
    }

    fun batchValueChange(valueList: MutableCollection<AutoData>) {
        if (!ClumsyCatEngine.inst().conf.auto.isOpen) {
            return
        }
        //提交到channel
        Utils.autoSaveScope.launch {
            for (record in valueList) {
                changeChannel.send(record)
            }
        }
    }

    suspend fun <T> Channel<T>.receiveBatch(maxBatchSize: Int): List<T> {
        val list = mutableListOf<T>()
        repeat(maxBatchSize) {
            val result = tryReceive()
            if (result.isSuccess) {
                list.add(result.getOrThrow())
            } else {
                return list // channel 没数据 立即返回
            }
        }
        return list
    }

    private suspend fun transferN() {

        val autoConf = ClumsyCatEngine.inst().conf.auto

        //深拷贝对象 转移数据
        val receiveList = changeChannel.receiveBatch(autoConf.batchCount)

        for (data in receiveList) {
            val state = data.state()
            val copyData = DeepCopyUtil.deepCopy(data)
            this.copyDataMap.put(copyData, data)
            when (state) {
                Option.NONE -> {
                    continue
                }

                Option.UPDATE -> {
                    val list = updateMap.computeIfAbsent(copyData.javaClass) { ArrayList() }
                    list.add(copyData)
                }

                Option.INSERT -> {
                    val list = insertMap.computeIfAbsent(copyData.javaClass) { ArrayList() }
                    list.add(copyData)
                }

                Option.DELETE -> {
                    val list = deleteMap.computeIfAbsent(copyData.javaClass) { ArrayList() }
                    list.add(copyData)
                }
            }
        }
    }

    private suspend fun transfer0() {

        val flushLock = LockCache.flushLock()

        flushLock.writeLock()

        try {
            //深拷贝对象 转移数据
            transferN()
        } finally {
            flushLock.writeUnlock()
        }
    }

    private suspend fun save() {

        for (entry in insertMap.entries) {
            val clazz = entry.key
            val dataList = entry.value
            try {
                ClumsyCatEngine.inst().storage.batchSave(clazz, dataList)
                removeCopy(dataList)
            } catch (throwable: Throwable) {
                Log.STORAGE.error("batchSave失败, ", throwable)
                //失败了 单个重试
                for (data in dataList) {
                    try {
                        ClumsyCatEngine.inst().storage.save(clazz, data)
                        removeCopy(data)
                    } catch (throwable: Throwable) {
                        Log.STORAGE.error("save失败, ", throwable)
                        //失败处理
                    }
                }
            }
        }

        for (entry in updateMap.entries) {
            val clazz = entry.key
            val dataList = entry.value
            try {
                ClumsyCatEngine.inst().storage.batchSave(clazz, dataList)
                removeCopy(dataList)
            } catch (throwable: Throwable) {
                Log.STORAGE.error("batchSave失败, ", throwable)
                //失败了 单个重试
                for (data in dataList) {
                    try {
                        ClumsyCatEngine.inst().storage.save(clazz, data)
                        removeCopy(data)
                    } catch (throwable: Throwable) {
                        Log.STORAGE.error("save失败, ", throwable)
                        //失败处理
                    }
                }
            }
        }

        for (entry in deleteMap.entries) {
            val clazz = entry.key
            val dataList = entry.value
            try {
                ClumsyCatEngine.inst().storage.batchSave(clazz, dataList)
                removeCopy(dataList)
            } catch (throwable: Throwable) {
                Log.STORAGE.error("batchSave失败, ", throwable)
                //失败了 单个重试
                for (data in dataList) {
                    try {
                        ClumsyCatEngine.inst().storage.save(clazz, data)
                        removeCopy(data)
                    } catch (throwable: Throwable) {
                        Log.STORAGE.error("save失败, ", throwable)
                        //失败处理
                    }
                }
            }
        }

    }

    private fun removeCopy(copyDataList: List<AutoData>) {
        for (copyData in copyDataList) {
            removeCopy(copyData)
        }
    }

    private fun removeCopy(copyData: AutoData) {
        val originData = this.copyDataMap.remove(copyData)
        if (originData == null) {
            return
        }
        DataOption.clear(originData)
    }

    fun start() {
        val auto = ClumsyCatEngine.inst().conf.auto

        Utils.autoSaveScope.launch {

            // 协程取消时自动退出
            while (isActive) {

                //从channel拷贝转移数据到saveMap
                transferN()

                //加锁 保证一个完整的检查点
                transfer0()

                //存库
                save()

                delay(auto.internal) // 每秒执行一次
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