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

        suspend fun inst(): MemberTable {
            return Tables.inst().getTable<MemberData>("MemberTable") as MemberTable
        }

    }

}