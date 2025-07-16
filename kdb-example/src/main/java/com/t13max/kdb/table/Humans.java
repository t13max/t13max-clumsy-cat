package com.t13max.kdb.table;

import com.t13max.kdb.bean.Human;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家表 自动生成
 *
 * @author t13max
 * @since 18:39 2025/7/15
 */
public class Humans {

    //有点蠢...
    public static Human get(long id, CoroutineContext coroutineContext) {
        Continuation<Human> continuation = new Continuation<Human>() {
            @Override
            public void resumeWith(@NotNull Object result) {

            }

            @NotNull
            @Override
            public CoroutineContext getContext() {
                return coroutineContext;
            }
        };

        return (Human) _Table.getInstance().humans.get(id, continuation);
    }

    public static void insert(Human human) {
        return _Table.getInstance().humans.insert(human);
    }

    public static Human newHuman() {
        return new Human();
    }
}
