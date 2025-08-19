package com.t13max.cc.conf;

/**
 * 缓存配置
 *
 * @author t13max
 * @since 17:56 2025/8/16
 */
public class CacheConf {

    private boolean open;

    private String instance;

    private int expired;

    private long cleanInterval;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(int expired) {
        this.expired = expired;
    }

    public long getCleanInterval() {
        return cleanInterval;
    }

    public void setCleanInterval(long cleanInterval) {
        this.cleanInterval = cleanInterval;
    }
}
