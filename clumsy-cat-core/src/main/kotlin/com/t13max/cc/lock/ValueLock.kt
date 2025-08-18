package com.t13max.cc.lock

import ReentrantMutex

/**
 * 记录锁
 * 协程不可重入 需要解决重入问题
 * @author t13max
 * @since 11:41 2025/7/9
 */
class ValueLock(private val name: String, private val lock: ReentrantMutex) {

    suspend fun <T> withLock(block: suspend () -> T): T {
        return lock.withLock { block() }
    }

    suspend fun lock() {
        lock.lock()
    }

    suspend fun unlock() {
        lock.unlock()
    }

    fun getName(): String {
        return name
    }
}