package com.t13max.kdb.transaction;

import com.t13max.kdb.log.IVarLog;
import com.t13max.kdb.log.LogKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查点
 *
 * @author t13max
 * @since 15:32 2025/7/10
 */
public class Savepoint {

    private final Map<LogKey, IVarLog<?>> logMap = new HashMap<>();

    private final List<IVarLog<?>> orderList = new ArrayList<>();

    public int commit() {
        for (IVarLog<?> varLog : orderList) {
            varLog.commit();
        }
        return orderList.size();
    }

    public int rollback() {
        for (IVarLog<?> varLog : orderList) {
            varLog.rollback();
        }
        return orderList.size();
    }
}
