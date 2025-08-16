package com.t13max.cc;

import com.t13max.cc.bean.AutoData;
import com.t13max.cc.conf.AutoConf;
import com.t13max.cc.conf.KdbConf;
import com.t13max.cc.enhance.SetterEnhancer;
import com.t13max.cc.executor.AutoSaveExecutor;
import com.t13max.cc.storage.IStorage;
import com.t13max.cc.storage.RegisterStorage;
import com.t13max.cc.table.Tables;
import com.t13max.cc.utils.Log;
import com.t13max.cc.utils.PackageUtil;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.util.Set;

/**
 * 核心类
 *
 * @author t13max
 * @since 17:01 2025/7/7
 */
public class KdbEngine {

    //配置文件变量名
    private final static String CONF_NAME = "CONF_NAME";

    //Kdb实例
    private final static KdbEngine INSTANCE = new KdbEngine();

    //配置
    private KdbConf CONF;

    //数据存储层接口
    @Getter
    private IStorage storage;

    /**
     * 获取实例
     *
     * @Author t13max
     * @Date 14:46 2025/7/15
     */
    public static KdbEngine inst() {
        return INSTANCE;
    }

    /**
     * 启动!
     *
     * @Author t13max
     * @Date 11:38 2025/7/12
     */
    public boolean start() {

        try {

            //加载配置
            loadConf();

            //实体类字节码增强
            dataEnhance();

            //初始化存储层
            initStorage();

            //初始化自动存库
            initAutoSave();

            //表集合
            Tables.inst().start(CONF);

        } catch (Throwable throwable) {
            Log.KDB.error("KDB start failed!", throwable);
            return false;
        }
        return true;
    }

    private void initAutoSave() {
        AutoConf autoConf = CONF.getAuto();
        if (!autoConf.isOpen()) {
            return;
        }

        //一些操作

        AutoSaveExecutor.Companion.inst().start();
    }

    private void initStorage() {
        String instance = CONF.getStorage().getInstance();
        switch (instance) {
            case "RegisterStorage" -> {
                this.storage = RegisterStorage.inst();
            }
            default -> {
                Log.KDB.error("存储层未实现");
            }
        }
    }

    /**
     * 字节码增强
     *
     * @Author t13max
     * @Date 18:28 2025/8/16
     */
    private void dataEnhance() throws Exception {
        String path = CONF.getData().getPath();
        Set<Class<?>> scan = PackageUtil.scan(path);
        for (Class<?> clazz : scan) {
            if (!AutoData.class.isAssignableFrom(clazz)) {
                continue;
            }
            SetterEnhancer.enhance(clazz.getName());
        }
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

            AutoSaveExecutor.Companion.inst().shutdown();

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

        String confName = System.getProperty(CONF_NAME, "kdb.yml");

        Yaml yaml = new Yaml();

        CONF = yaml.loadAs(KdbEngine.class.getClassLoader().getResourceAsStream(confName), KdbConf.class);
    }
}
