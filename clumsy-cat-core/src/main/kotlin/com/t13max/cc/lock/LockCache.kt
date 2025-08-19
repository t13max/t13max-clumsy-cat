package com.t13max.cc.lock

import ReentrantMutex
import WeakConcurrentMap
import com.t13max.cc.utils.CoroutineReadWriteLock
import java.util.concurrent.ConcurrentHashMap

/**
 * 锁缓存
 * todo atb 死锁检测 打断死锁
 *
 * @author t13max
 * @since 11:48 2025/7/9
 */
class LockCache {

    companion object {

        //外层ConcurrentHashMap 竞争不激烈 就是路由 里面是弱引用并发集合 分段锁
        private val cacheMap = ConcurrentHashMap<String, WeakConcurrentMap<Long, ValueLock>>()

        //刷库读写锁
        private val flushLock = CoroutineReadWriteLock()

        suspend fun getLock(name: String, id: Long): ValueLock {
            val lockMap = cacheMap.computeIfAbsent(name) { WeakConcurrentMap { ValueLock(name, ReentrantMutex()) } }
            return lockMap.get(id)
        }

        fun flushLock(): CoroutineReadWriteLock {
            return flushLock
        }

    }

}