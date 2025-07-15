package com.t13max.kdb;

import com.t13max.kdb.procedure.PTest;

import java.util.concurrent.CountDownLatch;

/**
 * @author t13max
 * @since 11:38 2025/7/12
 */
public class Main {

    private final static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{

        new PTest().submit();

        countDownLatch.await();
    }
}
