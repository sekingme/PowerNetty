 /* power by sekingme */

package org.infraRpcExample.config;

import lombok.Builder;
import org.infraRpcExample.utils.Constants;

/**
 * netty client config
 *
 * @author sekingme
 */
@Builder
public class NettyClientConfig {

    /**
     * worker threads，default get machine cpus
     */
    @Builder.Default
    private int workerThreads = Constants.CPUS;

    /**
     * whether tpc delay
     */
    @Builder.Default
    private boolean tcpNoDelay = true;

    /**
     * whether keep alive
     */
    @Builder.Default
    private boolean soKeepalive = true;

    /**
     * send buffer size
     */
    @Builder.Default
    private int sendBufferSize = 65535;

    /**
     * receive buffer size
     */
    @Builder.Default
    private int receiveBufferSize = 65535;

    /**
     * connect timeout millis
     */
    @Builder.Default
    private int connectTimeoutMillis = 3000;

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isSoKeepalive() {
        return soKeepalive;
    }

    public void setSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }
}
