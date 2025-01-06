 /* power by sekingme */

package org.infraRpcExample.host.hostmanager.interfaces;


import org.infraRpcExample.host.Host;
import org.infraRpcExample.protocol.RpcRequest;

/**
 * host manager
 *
 * @author sekingme
 */
public interface HostManager {

    /**
     * select host
     *
     * @param rpcRequest rpcRequest
     * @return host
     */
    Host select(RpcRequest rpcRequest);

}
