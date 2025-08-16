package com.t13max.cc.storage;

import com.t13max.cc.bean.IData;
import com.t13max.cc.utils.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 注册方法的Storage
 * 具体实现任意
 * 可以是查库 也可以是查redis
 * 线程不安全 理论上这些是一上来就注册好的 不存在并发
 *
 * @author t13max
 * @since 15:37 2025/8/16
 */
public final class RegisterStorage implements IStorage {

    //单例
    private volatile static RegisterStorage INSTANCE;

    private RegisterStorage() {
    }

    //注册的函数
    private final Map<Class<? extends IData>, Map<Method, Function<Long, ? extends IData>>> findByIdFunctionMap = new HashMap<>();

    public static RegisterStorage inst() {
        if (INSTANCE == null) {
            synchronized (RegisterStorage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RegisterStorage();
                }
            }
        }
        return INSTANCE;
    }

    public <T extends IData> void registerFunction(Class<T> clazz, Method method, Function<Long, T> function) {
        Map<Method, Function<Long, ? extends IData>> methodFunctionMap = findByIdFunctionMap.computeIfAbsent(clazz, k -> new HashMap<>());
        methodFunctionMap.computeIfAbsent(method, k -> function);
    }

    @Override
    public <T extends IData> List<T> findAll(Class<T> clazz) {
        return List.of();
    }

    @Override
    public <T extends IData> List<T> findByForeignId(Class<T> clazz, long foreignId, String foreignName) {
        return List.of();
    }

    @Override
    public <T extends IData> T findById(Class<T> clazz, long id) {
        Map<Method, Function<Long, ? extends IData>> methodFunctionMap = findByIdFunctionMap.get(clazz);
        if (methodFunctionMap == null) {
            Log.ENGINE.error("类型未注册, clazz={}", clazz);
            return null;
        }
        Function<Long, ? extends IData> function = methodFunctionMap.get(Method.FIND_BY_ID);
        if (function == null) {
            Log.ENGINE.error("函数未注册, clazz={}, method={}", clazz, Method.FIND_BY_ID);
            return null;
        }
        IData value = function.apply(id);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        //错误处理
        return null;
    }

    @Override
    public <T extends IData> void save(Class<T> clazz, T t) {

    }

    @Override
    public <T extends IData> void batchSave(Class<T> clazz, List<T> dataList) {

    }

    @Override
    public <T extends IData> void delete(Class<T> clazz, long id) {

    }

    @Override
    public <T extends IData> void batchDelete(Class<T> clazz, List<Long> ids) {

    }

    public enum Method {
        FIND_ALL,
        FIND_BY_ID,
        FIND_BY_FOREIGN_ID,
        SAVE,
        BATCH_SAVE,
        DELETE,
        BATCH_DELETE,
        ;
    }
}
