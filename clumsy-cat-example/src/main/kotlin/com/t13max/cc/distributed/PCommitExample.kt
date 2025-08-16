package com.t13max.cc.distributed

import com.t13max.cc.transaction.Procedure

class PCommitExample : Procedure() {

    override suspend fun process(): Boolean {
        //commit逻辑

        return true
    }
}