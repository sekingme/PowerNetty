 /* power by sekingme */

package org.infraRpcExample.host.hostmanager;

import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.host.selector.RandomSelector;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;

/**
 * random host manager
 *
 * @author sekingme
 */
public class RandomHostManager extends CommonHostManager {

    /**
     * selector
     */
    private final RandomSelector selector;

    /**
     * set round robin
     */
    public RandomHostManager() {
        this.selector = new RandomSelector();
    }

    @Override
    public HostWorker select(Collection<HostWorker> nodes, RpcRequest request) {
        return selector.select(nodes, request);
    }

}
