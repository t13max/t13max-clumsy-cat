package com.t13max.cc.cache

import com.t13max.cc.ClumsyCatEngine
import com.t13max.cc.bean.IData
import com.t13max.cc.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 表缓存管理器
 *
 * @author t13max
 * @since 16:18 2025/8/18
 */
class TableCacheManager {

    companion object {

        //单例
        private var INSTANCE: TableCacheManager = TableCacheManager()

        fun inst(): TableCacheManager {
            return INSTANCE
        }
    }

    private val cacheMap = mutableMapOf<String, ITableCache<IData>>()

    fun register(name: String, tableCache: ITableCache<IData>) {
        this.cacheMap.put(name, tableCache)
    }

    fun start() {

        val cacheConf = ClumsyCatEngine.inst().conf.cache
        val cleanInterval = cacheConf.cleanInterval

        Utils.tableCacheScope.launch {
            while (isActive) {
                //定期清除
                for (tableCache in cacheMap.values) {
                    tableCache.clean()
                }
                // 每隔1秒执行一次
                delay(cleanInterval)
            }
        }
    }

    fun shutdown() {
        Utils.tableCacheScope.coroutineContext[Job]?.cancel()
    }
}