package com.t13max.cc.bean;

/**
 * 数据操作选项
 *
 * @author t13max
 * @since 15:22 2025/7/15
 */
public enum Option {

    NONE(0), UPDATE(1), INSERT(2), DELETE(4);

    public final int code;

    private Option(int code) {
        this.code = code;
    }

    public boolean match(int op) {
        return (this.code & op) != 0;
    }

}
