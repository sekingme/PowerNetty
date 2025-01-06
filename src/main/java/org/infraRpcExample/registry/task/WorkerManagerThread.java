 /* power by sekingme */

package org.infraRpcExample.registry.task;

import org.springframework.stereotype.Component;

/**
 * Manage tasks 后续删除
 *
 * @author sekingme
 */
@Component
@Deprecated
public class WorkerManagerThread implements Runnable {
    @Override
    public void run() {
        Thread.currentThread().setName("Worker-Execute-Manager-Thread");
    }
}
