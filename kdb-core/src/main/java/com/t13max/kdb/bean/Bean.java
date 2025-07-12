package com.t13max.kdb.bean;


/**
 * @author t13max
 * @since 16:03 2025/7/7
 */
public abstract class Bean implements IBean {

    protected final Bean parent;

    protected final String varName;

    protected Bean(Bean parent, String varName) {
        this.parent = parent;
        this.varName = varName;
    }

    @Override
    public IBean parent() {
        return this.parent;
    }

    @Override
    public String getVarName() {
        return this.varName;
    }
}
