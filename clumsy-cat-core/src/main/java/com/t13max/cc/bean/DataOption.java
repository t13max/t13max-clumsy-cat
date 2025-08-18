package com.t13max.cc.bean;

/**
 * Option工具类
 *
 * @author t13max
 * @since 16:46 2025/8/18
 */
public class DataOption {

    public static <T extends IData> void insert(T value) {
        AutoData.insert(value);
    }

    public static <T extends IData> void delete(T value) {
        AutoData.delete(value);
    }

    public static <T extends IData> void clear(T value) {
        AutoData.clear(value);
    }
}
