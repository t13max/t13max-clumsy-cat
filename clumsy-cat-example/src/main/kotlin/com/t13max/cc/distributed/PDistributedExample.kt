package com.t13max.cc.distributed

import com.t13max.cc.transaction.DistributedProcedure

class PDistributedExample : DistributedProcedure() {

    override suspend fun tryProcess(): Boolean {
        //内嵌调用try逻辑
        return PTryExample().call()
    }

    override suspend fun commit(): Boolean {
        return PCommitExample().call()
    }



}