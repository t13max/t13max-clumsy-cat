package com.t13max.kdb.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author t13max
 * @since 16:11 2025/7/7
 */
public class Log {

    public static Logger SERIAL = LogManager.getLogger("SERIAL");
    public static Logger TRANSACT = LogManager.getLogger("TRANSACT");
    public static Logger KDB = LogManager.getLogger("KDB");
}
