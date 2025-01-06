 /* power by sekingme */

package org.infraRpcExample.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author sekingme
 * caller thread execute
 */
public class CallerThreadExecutePolicy implements RejectedExecutionHandler {

    private final Logger logger = LoggerFactory.getLogger(CallerThreadExecutePolicy.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.warn("queue is full, trigger caller thread execute");
        r.run();
    }
}
