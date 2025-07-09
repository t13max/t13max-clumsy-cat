package com.t13max.kdb.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 唯一id工具类
 *
 * @author t13max
 * @since 10:57 2025/7/8
 */
public class UuidUtils {

    //临时写一个
    private final static AtomicLong atomicLong = new AtomicLong();

    public static long getNextUuid() {
        return atomicLong.getAndIncrement();
    }
}
