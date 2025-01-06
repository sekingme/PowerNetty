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
 * @description: 随机选择节点
 * @create: 2024-09-25 17:04
 */
public class RandomSelector extends AbstractSelector<HostWorker> {

    @Override
    public HostWorker doSelect(final Collection<HostWorker> source, RpcRequest request) {

        List<HostWorker> hosts       = new ArrayList<>(source);
        int              size        = hosts.size();
        int[]            weights     = new int[size];
        int              totalWeight = 0;
        int              index       = 0;

        for (HostWorker host : hosts) {
            totalWeight += host.getWeight();
            weights[index] = host.getWeight();
            index++;
        }

        if (totalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);

            for (int i = 0; i < size; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return hosts.get(i);
                }
            }
        }
        return hosts.get(ThreadLocalRandom.current().nextInt(size));
    }

}
