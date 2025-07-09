package com.t13max.kdb.lock

import ReentrantMutex
import kotlinx.coroutines.sync.Mutex

/**
 * 锁缓存
 * 简单实现 未来可优化为弱引用并发集合
 *
 * @author t13max
 * @since 11:48 2025/7/9
 */
class LockCache {

    companion object{

        private val cacheMap = mutableMapOf<String, MutableMap<Long, RecordLock>>()

        private val lock = Mutex()

        suspend fun getLock(name: String, id: Long): RecordLock {

            lock.lock()

            try {
                val innerMap = cacheMap.getOrPut(name) { mutableMapOf() }

                return innerMap.getOrPut(id) {
                    RecordLock(name, ReentrantMutex())
                }
            } finally {
                lock.unlock()
            }
        }
    }

}