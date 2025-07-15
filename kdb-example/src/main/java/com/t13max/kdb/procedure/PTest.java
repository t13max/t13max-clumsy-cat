package com.t13max.kdb.procedure;

import com.t13max.kdb.bean.Human;
import com.t13max.kdb.table.Humans;
import com.t13max.kdb.transaction.Procedure;

/**
 * @author t13max
 * @since 17:24 2025/7/10
 */
public class PTest extends Procedure {


    /**
     * 逻辑
     *
     * @Author t13max
     * @Date 17:27 2025/7/10
     */
    protected boolean process() throws Exception {

        Human human = Humans.get(1);
        human.setExp(111);
        return false;
    }

}
