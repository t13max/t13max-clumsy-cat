package com.t13max.cc.storage;

import com.t13max.cc.bean.AutoData;
import com.t13max.cc.utils.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private final Map<Class<? extends AutoData>, Function<Long, AutoData>> findByIdFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, Supplier<List<AutoData>>> findAllFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, BiFunction<Long, String, List<AutoData>>> findByForeignIdFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, Consumer<AutoData>> saveFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, Consumer<List<AutoData>>> batchSaveFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, Consumer<Long>> deleteFunctionMap = new HashMap<>();

    private final Map<Class<? extends AutoData>, Consumer<List<Long>>> batchDeleteFunctionMap = new HashMap<>();

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

    public <T extends AutoData> void registerFindByIdFunction(Class<T> clazz, Function<Long, AutoData> function) {
        findByIdFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerFindAllFunction(Class<T> clazz, Supplier<List<AutoData>> function) {
        findAllFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerFindByForeignFunction(Class<T> clazz, BiFunction<Long, String, List<AutoData>> function) {
        findByForeignIdFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerSaveFunction(Class<T> clazz, Consumer<AutoData> function) {
        saveFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerBatchSaveFunction(Class<T> clazz, Consumer<List<AutoData>> function) {
        batchSaveFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerDeleteFunction(Class<T> clazz, Consumer<Long> function) {
        deleteFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    public <T extends AutoData> void registerBatchDeleteFunction(Class<T> clazz, Consumer<List<Long>> function) {
        batchDeleteFunctionMap.computeIfAbsent(clazz, k -> function);
    }

    @Override
    public <T extends AutoData> List<T> findAll(Class<T> clazz) {
        Supplier<List<AutoData>> function = findAllFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("findAll, 函数未注册, clazz={}, ", clazz);
            return null;
        }
        List<T> list = (List<T>)function.get();
        if (list == null) {
            return List.of();
        }
        return list;
    }

    @Override
    public <T extends AutoData> List<T> findByForeignId(Class<T> clazz, long foreignId, String foreignName) {
        BiFunction<Long, String, List<AutoData>> function = findByForeignIdFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("findByForeignId, 函数未注册, clazz={}, ", clazz);
            return null;
        }
        List<T> list = (List<T>)function.apply(foreignId,foreignName);
        if (list == null) {
            return List.of();
        }
        return list;
    }

    @Override
    public <T extends AutoData> T findById(Class<T> clazz, long id) {
       
        Function<Long, AutoData> function = findByIdFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("函数未注册, clazz={}, ", clazz);
            return null;
        }
        Object value = function.apply(id);
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
    public <T extends AutoData> void save(Class<T> clazz, AutoData data) {
        Consumer<AutoData> function = saveFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("save, 函数未注册, clazz={}, ", clazz);
            return;
        }
        function.accept(data);
    }

    @Override
    public <T extends AutoData> void batchSave(Class<T> clazz, List<AutoData> dataList) {
        Consumer<List<AutoData>> function = batchSaveFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("batchSave, 函数未注册, clazz={}, ", clazz);
            return;
        }
        function.accept(dataList);
    }

    @Override
    public <T extends AutoData> void delete(Class<T> clazz, long id) {
        Consumer<Long> function = deleteFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("delete, 函数未注册, clazz={}, ", clazz);
            return;
        }
        function.accept(id);
    }

    @Override
    public <T extends AutoData> void batchDelete(Class<T> clazz, List<Long> ids) {
        Consumer<List<Long>> function = batchDeleteFunctionMap.get(clazz);
        if (function == null) {
            Log.ENGINE.error("batchDelete, 函数未注册, clazz={}, ", clazz);
            return;
        }
        function.accept(ids);
    }

}
