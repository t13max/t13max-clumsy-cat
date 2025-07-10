package com.t13max.kdb.action;

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

    protected final long id;

    protected final boolean io;

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


}
