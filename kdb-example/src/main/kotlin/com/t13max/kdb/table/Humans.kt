package com.t13max.kdb.table

import com.t13max.kdb.bean.Human

/**
 *
 * @author t13max
 * @since 12:51 2025/8/11
 */
class Humans {


    //有点蠢...
    fun get(id: Long): Human? {
        return _Table.getInstance().humans.get(id)
    }

    fun insert(human: Human) {
        return _Table.getInstance().humans.insert(human)
    }

    fun newHuman(): Human {
        return Human()
    }
}