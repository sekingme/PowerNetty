 /* power by sekingme */

package org.infraRpcExample.host.selector;

import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: sekingme
 * @description: 根据权重选择合适节点
 * @create: 2024-09-25 17:23
 */
public class LowerWaitingTaskSelector extends AbstractSelector<HostWorker> {

    /**
     * select
     * 1、筛选等待任务数最小的节点
     * 2、筛选后，如果有多个节点，加权随机
     *
     * @param sources sources
     * @return HostWeight
     */
    @Override
    public HostWorker doSelect(Collection<HostWorker> sources, RpcRequest request) {
        List<HostWorker> hostWorkers    = new ArrayList<>(sources);
        int              length         = sources.size();
        int              minWaitTask    = Integer.MAX_VALUE;
        int              minCount       = 0;
        int[]            minIndexes     = new int[length];
        int              minTotalWeight = 0;
        int[]            weights        = new int[length];

        // 找到等待任务数最少的节点
        // todo 在负载信息更新前，等待任务数如果没有变化，可能导致在该时间片段内，所有的请求都到同一台服务器上
        for (int i = 0; i < length; i++) {
            HostWorker hostWorker  = hostWorkers.get(i);
            int        waitingTask = hostWorker.getHostWeight().getWaitingTaskCount();
            weights[i] = hostWorker.getWeight();
            if (waitingTask < minWaitTask) {
                minWaitTask = waitingTask;
                minCount = 1;
                minIndexes[0] = i;
                minTotalWeight = hostWorker.getWeight();
            } else if (waitingTask == minWaitTask) {
                minIndexes[minCount++] = i;
                minTotalWeight += hostWorker.getWeight();
            }
        }

        if (minCount == 1) {
            return hostWorkers.get(minIndexes[0]);
        }
        // 加权随机
        if (minTotalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(minTotalWeight);
            for (int i = 0; i < minCount; i++) {
                int index = minIndexes[i];
                offset -= weights[index];
                if (offset < 0) {
                    return hostWorkers.get(index);
                }
            }
        }
        return hostWorkers.get(minIndexes[ThreadLocalRandom.current().nextInt(minCount)]);
    }
}



