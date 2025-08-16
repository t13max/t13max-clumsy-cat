package com.t13max.kdb;

import com.t13max.kdb.conf.KdbConf;
import com.t13max.kdb.serial.SerialExecutor;
import com.t13max.kdb.table.Tables;
import com.t13max.kdb.transaction.TransactionExecutor;
import com.t13max.kdb.utils.Log;
import org.yaml.snakeyaml.Yaml;

/**
 * @author t13max
 * @since 17:01 2025/7/7
 */
public class Kdb {

    //配置文件变量名
    private final static String CONF_NAME = "CONF_NAME";

    //Kdb实例
    private final static Kdb INSTANCE = new Kdb();

    //配置
    private KdbConf CONF;

    //串行执行器
    private SerialExecutor serialExecutor;

    //事务执行器
    private TransactionExecutor transactionExecutor;

    /**
     * 获取实例
     *
     * @Author t13max
     * @Date 14:46 2025/7/15
     */
    public static Kdb getInstance() {
        return INSTANCE;
    }

    /**
     * 启动!
     *
     * @Author t13max
     * @Date 11:38 2025/7/12
     */
    public boolean start() throws Exception {

        try {

            //加载配置
            loadConf();

            //表集合
            Tables.inst().start(CONF);

            //串行执行器
            serialExecutor = new SerialExecutor();

            //事务执行器
            transactionExecutor = new TransactionExecutor();

        } catch (Throwable throwable) {
            Log.KDB.error("KDB start failed!", throwable);
            return false;
        }
        return true;
    }

    /**
     * 停止
     *
     * @Author t13max
     * @Date 14:47 2025/7/15
     */
    public boolean shutdown() {

        try {

            //挨个shutdown 是不是有问题 一个异常 后面都没法执行?

            Tables.inst().shutdown();

            serialExecutor.shutdown();

            transactionExecutor.shutdown();

            return true;
        } catch (Throwable throwable) {
            Log.KDB.error("kdb shutdown error!", throwable);
        }
        return false;
    }

    /**
     * 获取配置
     *
     * @Author t13max
     * @Date 14:47 2025/7/15
     */
    public KdbConf getConf() {
        return CONF;
    }

    /**
     * 加载配置
     *
     * @Author t13max
     * @Date 14:47 2025/7/15
     */
    private void loadConf() {

        String confName = System.getProperty("CONF_NAME", "kdb.yml");

        Yaml yaml = new Yaml();

        CONF = yaml.loadAs(Kdb.class.getClassLoader().getResourceAsStream(confName), KdbConf.class);
    }
}
