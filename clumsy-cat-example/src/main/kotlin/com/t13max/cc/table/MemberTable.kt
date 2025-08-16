package com.t13max.cc.table

import com.t13max.cc.cache.ITableCache
import com.t13max.cc.conf.TableConf
import com.t13max.cc.data.MemberData
import com.t13max.cc.storage.IStorage
import java.util.Optional

/**
 * 表对象 自动生成
 * member表
 *
 * @author t13max
 * @since 14:21 2025/8/15
 */
class MemberTable(tableConf: TableConf, cache: ITableCache<MemberData>, storage: IStorage) : Table<MemberData>(MemberData::class.java, tableConf, cache, storage) {

    companion object {

        suspend fun get(id: Long): Optional<MemberData> {
            var table = Tables.inst().getTable<MemberData>("MemberTable")
            var optional = table.get(id)
            return optional;
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