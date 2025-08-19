package com.t13max.cc.storage;

import com.t13max.cc.bean.AutoData;

import java.util.List;

/**
 * MySQL存储层
 *
 * @author t13max
 * @since 18:50 2025/7/15
 */
public class MySqlStorage implements IStorage {


    @Override
    public<T extends AutoData>  List<T> findAll(Class<T> clazz) {
        return List.of();
    }

    @Override
    public<T extends AutoData>  List<T> findByForeignId(Class<T> clazz, long foreignId, String foreignName) {
        return List.of();
    }

    @Override
    public<T extends AutoData>  T findById(Class<T> clazz, long id) {
        return null;
    }

    @Override
    public<T extends AutoData>  void save(Class<T> clazz, AutoData t) {

    }

    @Override
    public<T extends AutoData>  void batchSave(Class<T> clazz, List<AutoData> dataList) {

    }

    @Override
    public<T extends AutoData>  void delete(Class<T> clazz, long id) {

    }

    @Override
    public<T extends AutoData>  void batchDelete(Class<T> clazz, List<Long> ids) {

    }
}
