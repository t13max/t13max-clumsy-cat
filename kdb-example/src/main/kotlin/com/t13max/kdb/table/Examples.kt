package com.t13max.kdb.table

import com.t13max.kdb.cache.CoroutineSafeCache
import com.t13max.kdb.conf.TableConf
import com.t13max.kdb.data.ExampleData
import com.t13max.kdb.proxy.AutoDataProxyFactory
import com.t13max.kdb.storage.IStorage

/**
 * 表对象 自动生成
 *
 * @author t13max
 * @since 14:21 2025/8/15
 */
class Examples(clazz: Class<ExampleData>, tableConf: TableConf, cache: CoroutineSafeCache<ExampleData>, storage: IStorage) : Table<ExampleData>(clazz, tableConf, cache, storage) {

    companion object {

        suspend fun get(id: Long): ExampleData? {
            var table = Tables.inst().getTable<ExampleData>("examples")
            var value = table.get(id)
            if (value == null) {
                return null;
            }
            return AutoDataProxyFactory.createProxy<ExampleData>(value)
        }

        suspend fun newData(): ExampleData {
            //插入数据
            return ExampleData()
        }


    }

}