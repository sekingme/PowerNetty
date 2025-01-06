package org.infraRpcExample.host.hostmanager;

import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.host.selector.ConsistentHashSelector;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Yuanda Liao
 * Time:   2024/9/25 20:13
 * <p>
 * 一致性hash 管理器
 */
public class ConsistentHashHostManager extends CommonHostManager {

    /**
     * 一个服务对应一个哈希环
     */
    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    @Override
    protected HostWorker select(Collection<HostWorker> nodes, RpcRequest request) {
        String                 key              = request.getNodeType().getServerName();
        int                    identityHashCode = nodes.hashCode();
        ConsistentHashSelector selector         = selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            lock.lock();
            try {
                selector = selectors.get(key);
                if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
                    selectors.put(key, new ConsistentHashSelector(nodes, identityHashCode));
                }
            } finally {
                lock.unlock();
            }
            selector = selectors.get(key);
        }
        return selector.select(nodes, request);
    }
}
