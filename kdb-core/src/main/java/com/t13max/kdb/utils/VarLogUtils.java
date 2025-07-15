package com.t13max.kdb.utils;

import com.t13max.kdb.bean.IData;
import com.t13max.kdb.log.LogKey;
import com.t13max.kdb.transaction.Transaction;
import com.t13max.kdb.transaction.TransactionExecutor;
import kotlinx.coroutines.Job;
import lombok.experimental.UtilityClass;

/**
 * @author t13max
 * @since 11:20 2025/7/8
 */
@UtilityClass
public class VarLogUtils {


    public <T extends IData> void varChangeLog(Job job, LogKey logKey) {
        Transaction transaction = TransactionExecutor.Companion.getTransaction(job);
        transaction.addCache();
    }
}
