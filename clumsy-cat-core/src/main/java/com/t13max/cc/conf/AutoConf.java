package com.t13max.cc.conf;

/**
 * 自动存库配置
 *
 * @author t13max
 * @since 17:56 2025/8/16
 */
public class AutoConf {

    private boolean open;

    private long internal;

    private int batchCount;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public long getInternal() {
        return internal;
    }

    public void setInternal(long internal) {
        this.internal = internal;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

}
