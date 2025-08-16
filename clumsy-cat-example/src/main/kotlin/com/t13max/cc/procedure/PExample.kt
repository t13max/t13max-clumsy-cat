package com.t13max.cc.procedure

import com.t13max.cc.table.MemberTable
import com.t13max.cc.table.RoomTable
import com.t13max.cc.transaction.Procedure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *
 * @author t13max
 * @since 11:08 2025/8/16
 */
class PExample(private val uid: Long, private val roomId: Long) : Procedure() {

    override suspend fun process(): Boolean {

        //要保证获取顺序 防止死锁
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

        //同步存库操作
        executeWhileCommit(this::sync2DB)
        return false
        //return true
    }

    //可以是挂起函数
    private suspend fun sync2DB() {
        //协程等待
        withContext(Dispatchers.IO) {
            //IO操作
        }
    }
}