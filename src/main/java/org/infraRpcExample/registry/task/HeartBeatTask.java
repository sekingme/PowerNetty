 /* power by sekingme */

package org.infraRpcExample.registry.task;

import org.infraRpcExample.registry.client.RegistryClient;
import org.infraRpcExample.thread.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sekingme
 * 心跳检查任务：把节点的信息（cpu/内存/负载等）定时同步到注册中心
 */
public class HeartBeatTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);

    private final String         heartBeatPath;
    private final RegistryClient registryClient;
    private final String         serverType;
    private final HeartBeat      heartBeat;

    public HeartBeatTask(long startupTime,
                         double maxCpuloadAvg,
                         double reservedMemory,
                         int hostWeight,
                         String heartBeatPath,
                         String serverType,
                         RegistryClient registryClient,
                         int workerThreadCount
    ) {
        this.heartBeatPath = heartBeatPath;
        this.registryClient = registryClient;
        this.serverType = serverType;
        this.heartBeat = new HeartBeat(startupTime, maxCpuloadAvg, reservedMemory, hostWeight, workerThreadCount);
    }

    public String getHeartBeatInfo() {
        return this.heartBeat.encodeHeartBeat();
    }

    @Override
    public void run() {
        try {
            // check dead or not in zookeeper
            if (registryClient.checkIsDeadServer(heartBeatPath, serverType)) {
                registryClient.getStoppable().stop("I was judged to death, release resources and stop myself");
                return;
            }

            // update waiting task count
            heartBeat.setWorkerWaitingTaskCount(ThreadPoolManager.INSTANCE.getQueueSize());

            registryClient.persistEphemeral(heartBeatPath, heartBeat.encodeHeartBeat());
        } catch (Throwable ex) {
            logger.error("error write heartbeat info", ex);
        }
    }
}
