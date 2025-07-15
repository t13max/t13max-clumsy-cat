package com.t13max.kdb.task;

import com.t13max.kdb.serial.SerialTask;

/**
 * @author t13max
 * @since 17:27 2025/7/10
 */
public class STest extends SerialTask {

    public STest(long id) {
        super(id);
    }

    @Override
    protected boolean process() {
        return false;
    }
}
