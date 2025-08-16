package com.t13max.cc.conf;

/**
 * 自动存库配置
 *
 * @author t13max
 * @since 17:56 2025/8/16
 */
public class AutoConf {

    private boolean open;

    private int internal;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getInternal() {
        return internal;
    }

    public void setInternal(int internal) {
        this.internal = internal;
    }
}
