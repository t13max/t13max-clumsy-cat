package com.t13max.kdb.bean;

import com.t13max.kdb.exception.KdbException;
import com.t13max.kdb.lock.LockCache;
import kotlinx.coroutines.Job;

/**
 * 自动存库回滚 对象
 *
 * @author t13max
 * @since 15:28 2025/7/15
 */
public abstract class AutoData implements IData {

    private volatile byte _option;

    /**
     * 校验
     *
     * @Author t13max
     * @Date 16:02 2025/7/15
     */
    protected void verify() {

        if (job == null) {
            throw new KdbException("job is null");
        }

        if (!LockCache.Companion.flushLock().hasReadLock(job)) {
            throw new KdbException("don't has read lock");
        }
    }

    protected void clear() {
        _option = 0;
        job = null;
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

    public <V extends IData> V setJob(Job job) {
        this.job = job;
        return (V) this;
    }
}
