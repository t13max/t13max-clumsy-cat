package com.t13max.kdb.storage;

import com.t13max.kdb.bean.IData;

import java.util.List;

/**
 * MongoDB存储层
 *
 * @author t13max
 * @since 18:50 2025/7/15
 */
public class MongoStorage implements IStorage {

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
