package com.t13max.kdb.serial

import com.t13max.kdb.consts.Const

/**
 * 串行任务
 *
 * @author t13max
 * @since 14:15 2025/8/16
 */
abstract class SerialTask(
    val id: Long,
    val io: Boolean = false,
    val scope: String = Const.SCOPE_ROLE
) : Runnable {

    constructor(id: Long) : this(id, false, Const.SCOPE_ROLE)

    constructor(id: Long, io: Boolean) : this(id, io, Const.SCOPE_ROLE)

    final override fun run() {
        try {
            if (process()) {
                commit()
                return
            }
            rollback()
        } catch (t: Throwable) {
            rollback()
        } finally {
            finished()
        }
    }

    private fun finished() {

    }

    private fun rollback() {

    }

    private fun commit() {

    }

    protected abstract fun process(): Boolean
}