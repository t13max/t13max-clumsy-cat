package com.t13max.cc.cache;

import com.t13max.cc.bean.IData;
import lombok.experimental.UtilityClass;

/**
 * 表缓存工厂
 *
 * @author t13max
 * @since 11:01 2025/8/19
 */
@UtilityClass
public class TableCacheFactory {

    public static <T extends IData> ITableCache<T> createTableCache(String instance) {
        if (instance == null) {
            instance = "EmptyTableCache";
        }
        switch (instance) {
            case "DefaultTableCache" -> {
                return new DefaultTableCache<>();
            }
            default -> {
                return new EmptyTableCache<>();
            }
        }
    }
}
