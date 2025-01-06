package org.infraRpcExample.host.hostmanager;

import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.host.selector.RoundRobinSelector;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;

/**
 * @author Yuanda Liao
 * Time:   2024/10/8 11:47
 */
public class RoundRobinHostManager extends CommonHostManager {

    private final RoundRobinSelector selector = new RoundRobinSelector();

    @Override
    protected HostWorker select(Collection<HostWorker> nodes, RpcRequest request) {
        return selector.select(nodes, request);
    }

}
