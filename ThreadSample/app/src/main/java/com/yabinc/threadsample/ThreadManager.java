package com.yabinc.threadsample;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yabinc on 7/15/15.
 */
public class ThreadManager {

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final BlockingQueue<Runnable> mBlockingQueue = new LinkedBlockingQueue<Runnable>();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    ThreadPoolExecutor mThreadPoolExecutor;

    ThreadManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mBlockingQueue);
    }

    public void delayedRun(final int delayMillis, final Runnable runnable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long endTime = delayMillis + System.currentTimeMillis();
                while (System.currentTimeMillis() < endTime) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
                runnable.run();
            }
        }).start();
    }

    public void runOnThreadPool(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    public void runOnNewThread(final Runnable runnable) {
        new Thread(runnable).start();
    }
}
