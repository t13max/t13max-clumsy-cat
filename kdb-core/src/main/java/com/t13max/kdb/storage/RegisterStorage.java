package com.t13max.kdb.storage;

import com.t13max.kdb.bean.IData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 注册方法的Storage 具体实现任意
 *
 * @author t13max
 * @since 15:37 2025/8/16
 */
public class RegisterStorage implements IStorage {

    private final Map<Class<? extends IData>, Function<Long, ? extends IData>> findByIdFunctionMap = new HashMap<>();

    public <T extends IData> void registerFunction(Class<T> clazz, Function<Long, T> function) {
        findByIdFunctionMap.computeIfAbsent(clazz, k -> function);
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
        Function<Long, ? extends IData> function = findByIdFunctionMap.get(clazz);
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
}
