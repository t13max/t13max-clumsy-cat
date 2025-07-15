package com.t13max.kdb.table

import com.t13max.kdb.bean.Bean
import com.t13max.kdb.bean.IData
import com.t13max.kdb.bean.Record
import com.t13max.kdb.cache.CoroutineSafeCache
import com.t13max.kdb.cache.DefaultTableCache
import com.t13max.kdb.conf.TableConf
import com.t13max.kdb.lock.LockCache
import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.storage.IStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

/**
 * 数据表 从这里拿数据
 * 这里会协调查库 存库 等操作
 *
 * @author t13max
 * @since 18:50 2025/7/8
 */
open class Table<V : IData>(
    //类型
    private val clazz: Class<V>,
    //配置
    private val tableConf: TableConf,
    //表缓存
    private val cache : CoroutineSafeCache<V>,
    //存储层
    private val storage: IStorage,
) : Bean(null, null) {

    /**
     * 拿到一条数据
     *
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun get(id: Long): V? {

        val lock: RecordLock = LockCache.getLock(tableConf.name, id)

        //加锁执行
        lock.lock()
        try {
            var record = cache.get(id)
            if (record == null) {

                val outerTable = this@Table
                //IO操作 切换到IO线程执行
                record = withContext(Dispatchers.IO) {
                    val value = storage.findById(clazz, id)
                    Record(outerTable, value)
                }
            }
            return setJob(record.value)
        } finally {
            //释放吗?
            lock.unlock()
        }
    }

    suspend fun select(id: Long): V? {

        val lock: RecordLock = LockCache.getLock(tableConf.name, id)

        //加锁执行
        val result = lock.withLock {
            var record = cache.get(id)
            if (record == null) {
                val value = storage.findById(clazz, id)
                record = Record(this, value)
            }
            record
        }

        return setJob(result.value)
    }

    /**
     * 插入一条数据
     *
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun insert(value: V) {

        val lock: RecordLock = LockCache.getLock(tableConf.name, value.id)

        lock.lock()

        try {
            val record: Record<V> = Record(value)
            cache.add(record as Record<V?>?)
            setJob(value)
        } finally {
            lock.unlock()
        }
    }

    /**
     * 删除一条数据
     *
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun <V : IData> delete(id: Long) {
        // 实现删除逻辑
    }

    suspend fun <V : IData> setJob(v: V?): V? {
        if (v == null) {
            return null
        }
        val job = currentCoroutineContext()[Job]
        return v.setJob(job)
    }

    override fun getId(): Long? {
        return 0L
    }

}