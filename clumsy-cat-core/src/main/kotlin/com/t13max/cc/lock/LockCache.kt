package com.t13max.cc.lock

import ReentrantMutex
import WeakConcurrentMap
import com.t13max.cc.ClumsyCatEngine
import com.t13max.cc.utils.CoroutineReadWriteLock
import com.t13max.cc.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

        init {

            var conf = ClumsyCatEngine.inst().conf

            //死锁检测 开关
            if (conf.isDeadLockDetectOpen) {
                var deadLockDetectInterval = conf.deadLockDetectInterval

                Utils.commonScope.launch {

                    while (isActive) {
                        detectAndBreakDeadlocks()
                        delay(deadLockDetectInterval)
                    }
                }
            }

        }

        //死锁检测
        private fun detectAndBreakDeadlocks() {

            val graph = mutableMapOf<Long, MutableSet<Long>>()

            // 构建依赖图
            cacheMap.values.forEach { lockMap ->

                /*lockMap.forEach { (id, lock) ->
                    val ownerId = runCatching { lock.owner.getCompleted() }.getOrNull()
                    if (ownerId != null) {
                        lock.waiting.forEach { waitingId ->
                            graph.computeIfAbsent(waitingId) { mutableSetOf() }.add(ownerId)
                        }
                    }
                }*/
            }

            // 查找环
            val visited = mutableSetOf<Long>()
            val stack = mutableSetOf<Long>()

            fun hasCycle(v: Long): Boolean {
                if (stack.contains(v)) return true
                if (visited.contains(v)) return false
                visited.add(v)
                stack.add(v)
                graph[v]?.forEach { if (hasCycle(it)) return true }
                stack.remove(v)
                return false
            }

            graph.keys.forEach { id ->
                if (hasCycle(id)) {
                    println("Deadlock detected involving coroutine $id")
                    // 打断死锁协程
                    cancelCoroutineById(id)
                }
            }
        }

        fun cancelCoroutineById(id: Long) {
            //jobMap[id]?.cancel(CancellationException("Deadlock detected"))
        }
    }

}