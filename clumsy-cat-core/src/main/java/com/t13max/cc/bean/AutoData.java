package com.t13max.cc.bean;


import java.util.Map;

/**
 * 自动存库回滚 对象
 *
 * @author t13max
 * @since 15:28 2025/7/15
 */
public abstract class AutoData implements IData {

    private volatile byte _option;

    protected Map<String, Object> _oldValueMap;

    protected IData parent;

    protected void clear() {
        _option = 0;
    }

    protected byte option() {
        return _option;
    }

    protected void update() {
        _option = (byte) (_option | 1);
    }

    protected void insert() {
        _option = (byte) (_option | 2);
    }

    public Option state() {
        byte option = this.option();
        if (Option.INSERT.match(option)) {
            this.clear();
            return Option.INSERT;
        } else if (Option.UPDATE.match(option)) {
            this.clear();
            return Option.UPDATE;
        }
        return Option.NONE;
    }

}
