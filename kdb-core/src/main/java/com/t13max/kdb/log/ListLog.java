package com.t13max.kdb.log;

import java.util.List;

/**
 * @author t13max
 * @since 10:21 2025/7/11
 */
public class ListLog implements IVarLog<List<?>>{

    @Override
    public void rollback() {

    }

    @Override
    public void commit() {

    }

    @Override
    public boolean checkNoChange(List<?> objects) {
        return false;
    }
}
