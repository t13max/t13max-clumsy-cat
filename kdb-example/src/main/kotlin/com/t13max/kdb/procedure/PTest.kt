package com.t13max.kdb.procedure

import com.t13max.kdb.table.MemberTable
import com.t13max.kdb.table.RoomTable
import com.t13max.kdb.transaction.Procedure

/**
 *
 * @author t13max
 * @since 11:08 2025/8/16
 */
class PTest(val uid: Long, val roomId: Long) : Procedure() {


    override suspend fun process(): Boolean {

        val memberDataOp = MemberTable.get(uid)
        if (memberDataOp.isEmpty) {
            return false
        }

        val memberData = memberDataOp.get()

        val roomDataOp = RoomTable.get(roomId)
        if (roomDataOp.isEmpty) {
            return false
        }
        val roomData = roomDataOp.get()
        //一顿操作
        memberData.roomId = roomId

        roomData.uid = uid

        return false
        //return true
    }
}