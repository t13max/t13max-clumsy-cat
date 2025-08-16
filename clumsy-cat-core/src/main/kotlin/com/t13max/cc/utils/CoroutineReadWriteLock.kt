package com.t13max.cc.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

/**
 * 协程读写锁
 *
 * @Author t13max
 * @Date 11:54 2025/7/8
 */
class CoroutineReadWriteLock {

    // 保护共享状态的互斥锁
    private val stateMutex = Mutex()

    // 写锁（互斥）
    private val writeMutex = Mutex()

    private val readOwner = mutableSetOf<Job>()

    // 读锁的释放信号（用于唤醒等待的写者）
    private val readFinished = Channel<Unit>(Channel.UNLIMITED)

    // 当前活跃的读者数量
    private var readers = 0

    // 等待的写者数量
    private var waitingWriters = 0

    // 获取读锁
    suspend fun readLock(job: Job?) {
        stateMutex.lock()
        try {
            if (job != null) {
                readOwner.add(job)
            }
            // 如果有写者等待，暂停新读者（避免写者饥饿）
            if (waitingWriters > 0) {
                stateMutex.unlock()
                readFinished.receive() // 等待写者完成
                stateMutex.lock()
            }
            readers++
        } finally {
            stateMutex.unlock()
        }
    }

    // 释放读锁
    suspend fun readUnlock(job: Job?) {
        stateMutex.lock()
        try {
            if (job != null) {
                readOwner.remove(job)
            }
            if (--readers == 0 && waitingWriters > 0) {
                // 唤醒一个等待的写者
                readFinished.send(Unit)
            }
        } finally {
            stateMutex.unlock()
        }
    }

    // 获取写锁
    suspend fun writeLock() {
        stateMutex.lock()
        try {
            waitingWriters++
            // 如果当前有活跃读者，等待读完成
            if (readers > 0) {
                stateMutex.unlock()
                readFinished.receive() // 等待读完成
                stateMutex.lock()
            }
        } finally {
            // 获取底层写互斥锁
            writeMutex.lock()
            waitingWriters--
            stateMutex.unlock()
        }
    }

    // 释放写锁
    fun writeUnlock() {
        writeMutex.unlock()
        // 尝试唤醒其他等待者（读者或写者）
        if (waitingWriters == 0) {
            // 批量唤醒所有等待的读者
            var sent = 0
            while (sent < readers && readFinished.trySend(Unit).isSuccess) {
                sent++
            }
        }
    }

    /**
     * 判断当前是否持有flush读锁
     *
     * @Author t13max
     * @Date 16:43 2025/7/15
     */
    fun hasReadLock(job: Job): Boolean {
        return readOwner.contains(job)
    }
}