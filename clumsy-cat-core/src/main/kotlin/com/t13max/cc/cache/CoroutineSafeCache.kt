package com.t13max.cc.cache

import com.t13max.cc.ClumsyCatEngine
import com.t13max.cc.bean.IData
import com.t13max.cc.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * 协程安全 的表缓存 待完善
 *
 * @author t13max
 * @since 15:51 2025/7/9
 */
class CoroutineSafeCache<V : IData>() {

    private val cache = EmptyTableCache<V>()

    private val mutex = Mutex()

    suspend fun get(id: Long): V? {
        mutex.lock()
        try {
            return cache.get(id)
        } finally {
            mutex.unlock()
        }
    }

    //添加一条记录
    suspend fun add(value: V?) {
        mutex.lock()
        try {
            cache.add(value)
        } finally {
            mutex.unlock()
        }
    }

    //移除一条记录
    suspend fun remove(id: Long): V? {
        mutex.lock()
        try {
            return cache.remove(id)
        } finally {
            mutex.unlock()
        }
    }

}