package com.t13max.kdb.utils;

import com.t13max.kdb.bean.Bean;
import com.t13max.kdb.log.LogNotify;
import lombok.experimental.UtilityClass;

/**
 * @author t13max
 * @since 11:20 2025/7/8
 */
@UtilityClass
public class VarLogUtils {


    public static void logNotify(Bean kbean, LogNotify notify) {
        kbean.logNotify(notify);
    }
}
