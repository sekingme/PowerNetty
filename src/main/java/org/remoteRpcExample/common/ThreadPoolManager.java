 /* power by sekingme */

package org.remoteRpcExample.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

/**
 * @author Sekingme
 */

public enum ThreadPoolManager {

    INSTANCE;

    private static final int WORK_QUEUE_SIZE = 6;
    private static final long KEEP_ALIVE_TIME = 60;
    ExecutorService executorService;

    ThreadPoolManager() {
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                Runtime.getRuntime().availableProcessors() * 4, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(WORK_QUEUE_SIZE),
                new DiscardPolicy());
    }

    public void addExecuteTask(Runnable task) {
        executorService.submit(task);
    }
}
