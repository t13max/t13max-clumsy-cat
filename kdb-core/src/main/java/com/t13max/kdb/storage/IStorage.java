package com.t13max.kdb.storage;

import com.t13max.kdb.bean.IData;

import java.util.List;

/**
 * 数据存储层 顶级接口
 * 有多种实现 理论上支持MySQL MongoDB Redis等多种数据库
 *
 * @author t13max
 * @since 10:48 2025/7/8
 */
public interface IStorage {

    //查询所有
    <T extends IData> List<T> findAll(Class<T> clazz);

    //根据外键查询列表
    <T extends IData> List<T> findByForeignId(Class<T> clazz, long foreignId, String foreignName);

    //根据唯一id查询
    <T extends IData> T findById(Class<T> clazz, long id);

    //存
    <T extends IData> void save(Class<T> clazz, T t);

    //批量存
    <T extends IData> void batchSave(Class<T> clazz, List<T> dataList);

    //删除
    <T extends IData> void delete(Class<T> clazz, long id);

    //批量删
    <T extends IData> void batchDelete(Class<T> clazz, List<Long> ids);
}
