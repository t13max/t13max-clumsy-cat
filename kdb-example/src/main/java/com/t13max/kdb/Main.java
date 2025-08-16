package com.t13max.kdb;

import com.t13max.kdb.cache.EmptyTableCache;
import com.t13max.kdb.conf.TableConf;
import com.t13max.kdb.data.MemberData;
import com.t13max.kdb.data.RoomData;
import com.t13max.kdb.enhance.SetterEnhancer;
import com.t13max.kdb.procedure.PTest;
import com.t13max.kdb.storage.RegisterStorage;
import com.t13max.kdb.table.MemberTable;
import com.t13max.kdb.table.RoomTable;
import com.t13max.kdb.table.Tables;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author t13max
 * @since 11:38 2025/7/12
 */
public class Main {

    private final static CountDownLatch countDownLatch = new CountDownLatch(1);

    private final static Map<Long, MemberData> memberDataMap = new HashMap<>();
    private final static Map<Long, RoomData> roomDataHashMap = new HashMap<>();


    public static void main(String[] args) throws Exception {

        SetterEnhancer.enhance("com.t13max.kdb.data.MemberData");
        SetterEnhancer.enhance("com.t13max.kdb.data.RoomData");

        memberDataMap.put(1L, new MemberData(1L, "member xxx"));
        roomDataHashMap.put(2L, new RoomData(2L, "room xxxxxx"));


        RegisterStorage registerStorage = new RegisterStorage();
        registerStorage.registerFunction(MemberData.class, memberDataMap::get);
        registerStorage.registerFunction(RoomData.class, roomDataHashMap::get);



        Kdb.getInstance().start();

        Map<String, TableConf> confMap = new HashMap<>();
        for (TableConf tableConf : Kdb.getInstance().getConf().getTables()) {
            confMap.put(tableConf.getName(), tableConf);
        }

        Tables.inst().putTable("MemberTable", new MemberTable(confMap.get("MemberTable"), EmptyTableCache.emptyTableCache(), registerStorage));
        Tables.inst().putTable("RoomTable", new RoomTable(confMap.get("RoomTable"), EmptyTableCache.emptyTableCache(), registerStorage));

        new PTest(1L, 2L).submit();


        Thread.sleep(10000);

        System.out.println();
    }
}
