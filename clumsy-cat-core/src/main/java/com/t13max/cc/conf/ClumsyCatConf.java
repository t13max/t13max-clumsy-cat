package com.t13max.cc.conf;


import java.util.List;

/**
 * 引擎配置
 *
 * @author t13max
 * @since 10:24 2025/7/8
 */
public class ClumsyCatConf {

    //锁并发
    private int concurrencyLevel;
    //实体类路径
    private String dataPath;
    //表路径
    private String tablePath;

    //缓存相关配置
    private CacheConf cache;

    //自动存库相关配置
    private AutoConf auto;

    //存储层配置
    private StorageConf storage;

    //表配置 表名->配置
    private List<TableConf> tables;

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getTablePath() {
        return tablePath;
    }

    public void setTablePath(String tablePath) {
        this.tablePath = tablePath;
    }

    public CacheConf getCache() {
        return cache;
    }

    public void setCache(CacheConf cache) {
        this.cache = cache;
    }

    public AutoConf getAuto() {
        return auto;
    }

    public void setAuto(AutoConf auto) {
        this.auto = auto;
    }

    public StorageConf getStorage() {
        return storage;
    }

    public void setStorage(StorageConf storage) {
        this.storage = storage;
    }


    public List<TableConf> getTables() {
        return tables;
    }

    public void setTables(List<TableConf> tables) {
        this.tables = tables;
    }
}
