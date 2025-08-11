package com.t13max.kdb.table

import com.t13max.kdb.Kdb
import com.t13max.kdb.bean.Human
import com.t13max.kdb.cache.CoroutineSafeCache
import com.t13max.kdb.cache.DefaultTableCache
import com.t13max.kdb.conf.KdbConf
import com.t13max.kdb.storage.IStorage
import com.t13max.kdb.storage.MongoStorage

/**
 *
 * @author t13max
 * @since 12:52 2025/8/11
 */
class _Table : Tables{
    private static
    val INSTANCE: _Table = _Table(Kdb.getInstance().getConf())

    var humans: Table<Human>? = null

    fun _Table(kdbConf: KdbConf) {
        super(kdbConf)

        val storage: IStorage = MongoStorage()

        humans = Table<V?>(
            Human::class.java,
            kdbConf.tableConfMap.get("humans")!!,
            CoroutineSafeCache<Human>(DefaultTableCache<Human>()),
            storage
        )
    }

    fun getInstance(): _Table {
        return INSTANCE
    }
}