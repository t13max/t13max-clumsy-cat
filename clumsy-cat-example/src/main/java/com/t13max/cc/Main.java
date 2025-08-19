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

        //启动!
        if (!ClumsyCatEngine.inst().start()) {

            System.exit(0);
            return;
        }

        //注册storage操作
        RegisterStorage registerStorage = RegisterStorage.inst();
        registerStorage.registerFindByIdFunction(MemberData.class, memberDataMap::get);
        registerStorage.registerFindByIdFunction(RoomData.class, roomDataHashMap::get);


        //模拟数据库
        memberDataMap.put(1L, new MemberData(1L, "member xxx"));
        roomDataHashMap.put(2L, new RoomData(2L, "room xxxxxx"));

        //测试存储过程
        new PExample(1L, 2L).submit();


        Thread.sleep(10000);

        System.out.println();
    }
}
