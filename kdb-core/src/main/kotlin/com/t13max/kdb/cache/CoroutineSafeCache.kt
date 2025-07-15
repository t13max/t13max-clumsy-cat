package com.t13max.kdb.cache

import com.t13max.kdb.bean.IData
import com.t13max.kdb.bean.Record
import com.t13max.kdb.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * 协程安全 lru的缓存
 * 所有操作都要加锁 是否能优化成读写锁?
 *
 * @author t13max
 * @since 15:51 2025/7/9
 */
class CoroutineSafeCache<V : IData>(private val cache: ITableCache<V>) {

    private val mutex = Mutex()

    init {

        val job = Utils.tableCacheScope.launch {
            while (isActive) {

                //定期清除
                cache.clean()
                // 每隔1秒执行一次
                delay(1000L)
            }
        }
    }

    fun shutdown() {
        Utils.tableCacheScope.coroutineContext[Job]?.cancel()
    }

    suspend fun get(id: Long): Record<V?> {
        mutex.lock()
        try {
            return cache.get(id)
        } finally {
            mutex.unlock()
        }
    }

    //添加一条记录
    suspend fun add(record: Record<V?>?) {
        mutex.lock()
        try {
            cache.add(record)
        } finally {
            mutex.unlock()
        }
    }

    //移除一条记录
    suspend fun remove(id: Long): Record<V?> {
        mutex.lock()
        try {
            return cache.remove(id)
        } finally {
            mutex.unlock()
        }
    }

}