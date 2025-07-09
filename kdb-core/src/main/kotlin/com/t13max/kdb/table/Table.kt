package com.t13max.kdb.table

import com.t13max.kdb.bean.Bean
import com.t13max.kdb.bean.IData
import com.t13max.kdb.bean.Record
import com.t13max.kdb.cache.CoroutineSafeCache
import com.t13max.kdb.conf.TableConf
import com.t13max.kdb.lock.LockCache
import com.t13max.kdb.lock.RecordLock
import com.t13max.kdb.storage.IStorage

/**
 * 数据表 从这里拿数据
 * 这里会协调查库 存库 等操作
 *
 * @author t13max
 * @since 18:50 2025/7/8
 */
class Table<V : IData>(
    //类型
    private val clazz: Class<V>,
    //配置
    private val tableConf: TableConf,
    //表缓存
    private val cache: CoroutineSafeCache<V>,
    //存储层
    private val storage: IStorage,
) : Bean(null, null) {

    /**
     * 拿到一条数据
     *
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun <V : IData> get(id: Long): V? {

        val lock: RecordLock = LockCache.getLock(tableConf.name, id)

        //加锁执行
        lock.lock()
        try {
            var record = cache.get(id)
            if (record == null) {
                val value = storage.findById(clazz, id)
                record = Record(this, value)
            }
            return record.value as V
        } finally {
            //释放吗?
            lock.unlock()
        }
    }

    suspend fun <V : IData> select(id: Long): V? {

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

        return result.value as V
    }

    /**
     * 插入一条数据
     *
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun <V : IData> insert(value: V) {
        // 实现插入逻辑
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

    override fun getId(): Long? {
        return 0L
    }

}