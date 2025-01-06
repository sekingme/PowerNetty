 /* power by sekingme */
package org.infraRpcExample.config;

import lombok.Builder;
import org.infraRpcExample.utils.Constants;

/**
 * netty server config
 *
 * @author sekingme
 */
@Builder
public class NettyServerConfig {

    /**
     * init the server connectable queue
     */
    @Builder.Default
    private int soBacklog = 1024;

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
     * worker threads，default get machine cpus
     */
    @Builder.Default
    private int workerThread = Constants.CPUS;

    /**
     * default listen port
     */
    @Builder.Default
    private int listenPort = 8085;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public void setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
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

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }
}
