 /* power by sekingme */

package org.remoteRpcExample.config;


import org.remoteRpcExample.future.RpcFuture;

/**
 * Rpc Request Cache
 */
public class RpcRequestCache {

    private RpcFuture rpcFuture;

    private String serviceName;

    public RpcFuture getRpcFuture() {
        return rpcFuture;
    }

    public void setRpcFuture(RpcFuture rpcFuture) {
        this.rpcFuture = rpcFuture;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
