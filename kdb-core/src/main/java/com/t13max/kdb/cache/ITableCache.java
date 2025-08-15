package com.t13max.kdb.cache;

import com.t13max.kdb.bean.IData;

/**
 * 表缓存 顶级接口
 * 未来可能有多种缓存实现
 *
 * @author t13max
 * @since 10:52 2025/7/8
 */
public interface ITableCache<V extends IData> {

    //根据id取到数据
    V get(long id);

    //添加一条记录
    void add(V record);

    //移除一条记录
    V remove(long id);

    //清空
    void clear();

    //定期清理
    void clean();
}
