package org.infraRpcExample.host.selector;

import com.google.common.collect.Maps;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Yuanda Liao
 * Time:   2024/10/8 11:39
 */
public class RoundRobinSelector extends AbstractSelector<HostWorker> {

    private final Map<NodeType, Map<String, Long>> allCurrentWeightMap = Maps.newHashMap();

    private final Lock lock = new ReentrantLock();

    @Override
    protected HostWorker doSelect(Collection<HostWorker> source, RpcRequest request) {
        Map<String, HostWorker> addressHostMap = source.stream().collect(Collectors.toMap(Host::getAddress, h -> h));
        lock.lock();
        try {
            Map<String, Long> hostCurrentWeights = allCurrentWeightMap.computeIfAbsent(request.getNodeType(), k -> Maps.newHashMap());
            hostCurrentWeights.entrySet().removeIf(e -> !addressHostMap.containsKey(e.getKey()));

            long   totalWeight      = 0;
            long   maxCurrentWeight = -1L;
            String maxAddress       = null;

            for (String address : addressHostMap.keySet()) {
                long currentWeight = hostCurrentWeights.getOrDefault(address, 0L);
                currentWeight += addressHostMap.get(address).getWeight();
                hostCurrentWeights.put(address, currentWeight);
                totalWeight += currentWeight;
                if (currentWeight > maxCurrentWeight) {
                    maxCurrentWeight = currentWeight;
                    maxAddress = address;
                }
            }
            if (maxAddress != null) {
                hostCurrentWeights.put(maxAddress, maxCurrentWeight - totalWeight);
                return addressHostMap.get(maxAddress);
            }
        } finally {
            lock.unlock();
        }
        return source.iterator().next();
    }

}
