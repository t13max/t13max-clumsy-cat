package com.t13max.cc.bean;


import java.util.Map;

/**
 * 自动存库回滚 对象
 * todo atb 引用类型支持
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

    public byte option() {
        return _option;
    }

    protected void update() {
        _option = (byte) (_option | 1);
    }

    protected void insert() {
        _option = (byte) (_option | 2);
    }

    protected void delete() {
        _option = (byte) (_option | 4);
    }

    @Override
    public Option state() {
        byte option = this.option();
        if (Option.INSERT.match(option)) {
            return Option.INSERT;
        } else if (Option.UPDATE.match(option)) {
            return Option.UPDATE;
        } else if (Option.DELETE.match(option)) {
            return Option.DELETE;
        }
        return Option.NONE;
    }

    //插入数据 标记改变 包级私有
    static <T extends IData> void insert(T t) {
        if (t instanceof AutoData autoData) {
            autoData.insert();
        }
    }

    static <T extends IData> void delete(T t) {
        if (t instanceof AutoData autoData) {
            autoData.delete();
        }
    }

    static <T extends IData> void clear(T t) {
        if (t instanceof AutoData autoData) {
            autoData.clear();
        }
    }

}
