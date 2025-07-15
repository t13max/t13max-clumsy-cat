package com.t13max.kdb.bean;


/**
 * Bean抽象父类
 *
 * @author t13max
 * @since 16:03 2025/7/7
 */
public abstract class Bean implements IBean {

    //parent
    protected final Bean parent;

    //变量名
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
