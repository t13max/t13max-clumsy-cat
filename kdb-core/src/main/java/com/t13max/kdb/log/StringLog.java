package com.t13max.kdb.log;

/**
 * 字符串
 *
 * @author t13max
 * @since 15:41 2025/7/10
 */
public abstract class StringLog extends Note implements IVarLog<String> {

    protected LogKey logkey;

    protected String saved;

    protected StringLog(LogKey logkey, String saved) {
        this.logkey = logkey;
        this.saved = saved;
    }

    @Override
    public void commit() {

    }

}
