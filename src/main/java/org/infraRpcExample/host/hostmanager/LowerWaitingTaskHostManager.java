 /* power by sekingme */

package org.infraRpcExample.host.hostmanager;

import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.host.selector.LowerWaitingTaskSelector;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;

/**
 * @author: sekingme
 * @description: lower waiting task host manager
 * @create: 2024-09-25 17:51
 */
public class LowerWaitingTaskHostManager extends CommonHostManager {

    /**
     * selector
     */
    private final LowerWaitingTaskSelector selector = new LowerWaitingTaskSelector();

    @Override
    public HostWorker select(Collection<HostWorker> nodes, RpcRequest request) {
        return selector.select(nodes, request);
    }
}
