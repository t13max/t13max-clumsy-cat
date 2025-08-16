package com.t13max.cc.distributed

import com.t13max.cc.transaction.Procedure

class PCancelExample : Procedure() {

    override suspend fun process(): Boolean {
        //cancel逻辑

        return true
    }
}