package com.t13max.cc.distributed

import com.t13max.cc.transaction.Procedure

class PTryExample : Procedure() {

    override suspend fun process(): Boolean {
        //try逻辑

        return true
    }
}