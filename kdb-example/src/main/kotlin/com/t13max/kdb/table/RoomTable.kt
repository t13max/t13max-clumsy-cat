package com.t13max.kdb.table

import com.t13max.kdb.cache.CoroutineSafeCache
import com.t13max.kdb.cache.ITableCache
import com.t13max.kdb.conf.TableConf
import com.t13max.kdb.data.MemberData
import com.t13max.kdb.data.RoomData
import com.t13max.kdb.storage.IStorage
import java.util.Optional

/**
 * 表对象 自动生成
 * room表
 *
 * @author t13max
 * @since 14:21 2025/8/15
 */
class RoomTable(tableConf: TableConf, cache: ITableCache<RoomData>, storage: IStorage) : Table<RoomData>(RoomData::class.java, tableConf, cache, storage) {

    companion object {

        suspend fun get(id: Long): Optional<RoomData> {
            var table = Tables.inst().getTable<RoomData>("RoomTable")
            var optional = table.get(id)
            return optional
            /*if (optional.isEmpty) {
                return optional
            }
            return Optional.of(AutoDataProxyFactory.createProxy(optional.get()))*/
        }

        suspend fun newData(): MemberData {
            //插入数据
            return MemberData()
        }


    }

}