 /* power by sekingme */
package org.infraRpcExample.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sekingme
 * thread factory
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger increment = new AtomicInteger(1);

    /**
     * name
     */
    private final String name;

    /**
     * count
     */
    private final int count;

    public NamedThreadFactory(String name) {
        this(name, 0);
    }

    public NamedThreadFactory(String name, int count) {
        this.name = name;
        this.count = count;
    }

    /**
     * create thread
     *
     * @param r runnable
     * @return thread
     */
    @Override
    public Thread newThread(Runnable r) {
        final String threadName = count > 0 ? String.format("%s_%d_%d", name, count, increment.getAndIncrement())
                : String.format("%s_%d", name, increment.getAndIncrement());
        Thread t = new Thread(r, threadName);
        t.setDaemon(true);
        return t;
    }
}
