package com.t13max.kdb.table;

import com.t13max.kdb.Kdb;
import com.t13max.kdb.bean.Human;
import com.t13max.kdb.cache.CoroutineSafeCache;
import com.t13max.kdb.cache.DefaultTableCache;
import com.t13max.kdb.conf.KdbConf;
import com.t13max.kdb.storage.IStorage;
import com.t13max.kdb.storage.MongoStorage;

/**
 * 表 自动生成
 *
 * @author t13max
 * @since 18:42 2025/7/15
 */
public class _Table extends Tables {

    private final static _Table INSTANCE = new _Table(Kdb.getInstance().getConf());

    Table<Human> humans;

    public _Table(KdbConf kdbConf) {
        super(kdbConf);

        IStorage storage = new MongoStorage();

        humans = new Table<>(Human.class, kdbConf.tableConfMap.get("humans"), new CoroutineSafeCache<Human>(new DefaultTableCache<>()), storage);

    }

    public static _Table getInstance() {
        return INSTANCE;
    }
}
