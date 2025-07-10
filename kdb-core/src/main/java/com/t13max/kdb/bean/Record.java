package com.t13max.kdb.bean;

import com.t13max.kdb.consts.State;
import com.t13max.kdb.log.LogNotify;
import lombok.Getter;

/**
 * @author t13max
 * @since 16:57 2025/7/7
 */
@Getter
public class Record<V extends IData> extends Bean {

    public static final String RECORD_VAR_NAME = "value";

    private final V value;

    private State state;

    public Record(V value) {
        this(null, value);
    }

    public Record(Bean parent, V value) {
        super(parent, RECORD_VAR_NAME);
        this.value = value;
    }

    @Override
    public Long getId() {
        return value.getId();
    }

    @Override
    public void logNotify(LogNotify notify) {
        super.logNotify(notify);
    }
}
