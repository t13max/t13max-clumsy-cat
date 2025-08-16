package com.t13max.cc;

import com.t13max.cc.cache.EmptyTableCache;
import com.t13max.cc.conf.TableConf;
import com.t13max.cc.data.MemberData;
import com.t13max.cc.data.RoomData;
import com.t13max.cc.procedure.PExample;
import com.t13max.cc.storage.RegisterStorage;
import com.t13max.cc.table.MemberTable;
import com.t13max.cc.table.RoomTable;
import com.t13max.cc.table.Tables;

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



        memberDataMap.put(1L, new MemberData(1L, "member xxx"));
        roomDataHashMap.put(2L, new RoomData(2L, "room xxxxxx"));


        RegisterStorage registerStorage = RegisterStorage.inst();
        registerStorage.registerFunction(MemberData.class, RegisterStorage.Method.FIND_BY_ID, memberDataMap::get);
        registerStorage.registerFunction(RoomData.class, RegisterStorage.Method.FIND_BY_ID, roomDataHashMap::get);


        if (!ClumsyCatEngine.inst().start()) {

            System.exit(0);
        }

        Map<String, TableConf> confMap = new HashMap<>();
        for (TableConf tableConf : ClumsyCatEngine.inst().getConf().getTables()) {
            confMap.put(tableConf.getName(), tableConf);
        }

        Tables.inst().putTable("MemberTable", new MemberTable(confMap.get("MemberTable"), EmptyTableCache.emptyTableCache(), registerStorage));
        Tables.inst().putTable("RoomTable", new RoomTable(confMap.get("RoomTable"), EmptyTableCache.emptyTableCache(), registerStorage));

        new PExample(1L, 2L).submit();


        Thread.sleep(10000);

        System.out.println();
    }
}
