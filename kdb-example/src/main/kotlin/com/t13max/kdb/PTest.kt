package com.t13max.kdb

import com.t13max.kdb.table.Humans
import com.t13max.kdb.transaction.Procedure

/**
 *
 * @author t13max
 * @since 12:46 2025/8/11
 */
class PTest(private val a: Int, private val humanId: Long) : Procedure() {

    override suspend fun process(): Boolean {
        Humans.get(humanId)
        return super.process()
    }
}