package com.t13max.cc.table

import com.t13max.cc.bean.IData
import com.t13max.cc.cache.ITableCache
import com.t13max.cc.conf.TableConf
import com.t13max.cc.exception.CCException
import com.t13max.cc.lock.LockCache
import com.t13max.cc.lock.RecordLock
import com.t13max.cc.storage.IStorage
import com.t13max.cc.transaction.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Optional

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
    private val cache: ITableCache<V>,
    //存储层
    private val storage: IStorage,
) {

    /**
     * 拿到一条数据
     * 加协程锁
     * @Author t13max
     * @Date 18:56 2025/7/8
     */
    suspend fun get(id: Long): Optional<V> {

        val lock: RecordLock = LockCache.getLock(tableConf.name, id)

        //加锁执行
        lock.lock()

        val current = Transaction.current() ?: throw CCException("Transaction.current()为空")
        current.addLock(lock)
        try {
            //事务缓存
            var value = current.getCache(clazz, id)
            if (value == null) {
                //表缓存
                value = cache.get(id)
                if (value == null) {
                    //IO操作 切换到IO线程执行 这里要不要优化一下专门的IO
                    value = withContext(Dispatchers.IO) {
                        //持久层
                        val value = storage.findById(clazz, id)
                        value
                    }
                    if (value != null) {
                        cache.add(value)
                    }
                }
                if (value != null){
                    current.addCache(clazz,value)
                }
            }

            return Optional.ofNullable(value)
        } finally {
            //不释放锁
        }
    }

    suspend fun select(id: Long): V {

        val lock: RecordLock = LockCache.getLock(tableConf.name, id)

        //加锁执行
        lock.lock()
        try {
            var value = cache.get(id)
            if (value == null) {
                //IO操作 切换到IO线程执行 这里要不要优化一下专门的IO
                value = withContext(Dispatchers.IO) {
                    val value = storage.findById(clazz, id)
                    value
                }
            }
            return value
        } finally {

            lock.unlock()
        }

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
            cache.add(value)
            //通知异步存库 如果有
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

}