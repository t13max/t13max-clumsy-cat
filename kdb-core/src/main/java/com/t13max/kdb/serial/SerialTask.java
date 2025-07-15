package com.t13max.kdb.serial;

import com.t13max.kdb.consts.Const;
import lombok.Getter;

/**
 * 串行任务
 *
 * @author t13max
 * @since 11:24 2025/7/10
 */
@Getter
public abstract class SerialTask implements Runnable {

    //唯一id
    protected final long id;

    //是否存在IO操作
    protected final boolean io;

    //作用域
    protected final String scope;

    protected SerialTask(long id) {
        this(id, false, Const.SCOPE_ROLE);
    }

    protected SerialTask(long id, boolean io) {
        this(id, io, Const.SCOPE_ROLE);
    }

    protected SerialTask(long id, boolean io, String scope) {
        this.id = id;
        this.io = io;
        this.scope = scope;
    }

    @Override
    public final void run() {
        try {
            if (process()) {
                commit();
                return;
            }
            rollback();
        } catch (Throwable throwable) {
            rollback();
        } finally {
            finished();
        }
    }

    private void finished() {

    }

    private void rollback() {

    }

    private void commit() {

    }

    protected abstract boolean process();
}
