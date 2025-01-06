 /* power by sekingme */

package org.infraRpcExample.registry.impl.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.infraRpcExample.registry.ConnectionListener;
import org.infraRpcExample.registry.ConnectionState;
import org.slf4j.Logger;

/**
 * @author sekingme
 * @description:
 * @create: 2024-10-08 15:10
 */
public final class ZookeeperConnectionStateListener implements ConnectionStateListener {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ZookeeperConnectionStateListener.class);

    private final ConnectionListener listener;

    public ZookeeperConnectionStateListener(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void stateChanged(CuratorFramework client,
                             org.apache.curator.framework.state.ConnectionState newState) {
        switch (newState) {
            case LOST:
                log.warn("Registry disconnected");
                listener.onUpdate(ConnectionState.DISCONNECTED);
                break;
            case RECONNECTED:
                log.info("Registry reconnected");
                listener.onUpdate(ConnectionState.RECONNECTED);
                break;
            case SUSPENDED:
                log.warn("Registry suspended");
                listener.onUpdate(ConnectionState.SUSPENDED);
                break;
            default:
                break;
        }
    }
}
