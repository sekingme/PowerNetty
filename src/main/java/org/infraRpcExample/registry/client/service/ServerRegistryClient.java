 /* power by sekingme */

package org.infraRpcExample.registry.client.service;

import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.registry.ConnectionState;
import org.infraRpcExample.registry.client.RegistryClient;
import org.infraRpcExample.registry.task.HeartBeatTask;
import org.infraRpcExample.thread.IStoppable;
import org.infraRpcExample.thread.NamedThreadFactory;
import org.infraRpcExample.thread.ThreadUtils;
import org.infraRpcExample.utils.Constants;
import org.infraRpcExample.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.infraRpcExample.utils.Constants.*;


/**
 * @author: sekingme
 * @description: 不同服务注册统一入口：用于各个微服务的服务端分别注册
 * @create: 2024-09-25 17:04
 */
@Service
public class ServerRegistryClient {

    private final Logger logger = LoggerFactory.getLogger(ServerRegistryClient.class);

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    @Autowired
    private RegistryClient registryClient;

    @Value("${worker.exec.threads:100}")
    private int workerExecThreads;

    @Value("${worker.reserved.memory:0.3}")
    private double workerReservedMemory;

    @Value("${worker.host.weight:100}")
    private int hostWeight;

    @Value("${worker.heartbeat.interval:30}")
    private int workerHeartbeatInterval;

    /**
     * worker startup time, ms
     */
    private long startupTime;

    private String serverZkPath;
    private int    listenPort;

    @PostConstruct
    public void initServerRegistry() {
        this.startupTime = System.currentTimeMillis();
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     * registry
     * nodeType： 注册的服务类型
     * listenPort：注册的服务对应监听的端口
     */
    public void registry(NodeType nodeType, int listenPort) {
        String address      = NetUtils.getAddr(listenPort);
        String serverZkPath = getServerZkPath(nodeType.getServerName(), address);
        this.serverZkPath = serverZkPath;
        this.listenPort = listenPort;

        HeartBeatTask heartBeatTask = new HeartBeatTask(startupTime,
                DEFAULT_WORKER_CPU_LOAD,
                workerReservedMemory,
                hostWeight,
                serverZkPath,
                nodeType.getServerName(),
                registryClient,
                workerExecThreads
        );

        // remove before persist
        registryClient.remove(serverZkPath);
        registryClient.persistEphemeral(serverZkPath, heartBeatTask.getHeartBeatInfo());
        logger.info("server node : {} registry to ZK {} successfully", address, serverZkPath);

        while (!this.checkNodeExists(nodeType)) {
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 100ms
        ThreadUtils.sleep(Constants.HUNDRED_TIME_MILLIS);

        // delete dead server
        this.handleDeadServer(Collections.singleton(serverZkPath), nodeType, Constants.DELETE_OP);

        registryClient.addConnectionStateListener(this::handleConnectionState);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, workerHeartbeatInterval, workerHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("server node : {} heartbeat interval {} s", address, workerHeartbeatInterval);
    }

    public void handleConnectionState(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                logger.debug("registry connection state is {}", state);
                break;
            case SUSPENDED:
                logger.warn("registry connection state is {}, ready to retry connection", state);
                break;
            case RECONNECTED:
                logger.debug("registry connection state is {}, clean the node info", state);
                registryClient.persistEphemeral(this.serverZkPath, "");
                logger.info("server node : {} reconnect to ZK {} successfully", NetUtils.getAddr(this.listenPort), this.serverZkPath);
                break;
            case DISCONNECTED:
                logger.warn("registry connection state is {}, ready to stop myself", state);
                registryClient.getStoppable().stop("registry connection state is DISCONNECTED, stop myself");
                break;
            default:
        }
    }

    /**
     * remove registry info
     */
    public void unRegistry() throws IOException {
        try {
            registryClient.remove(this.serverZkPath);
            logger.info("server node : {} un registry from ZK {}.", getLocalAddress(), serverZkPath);
        } catch (Exception ex) {
            logger.error("remove server zk path exception", ex);
        }

        this.heartBeatExecutor.shutdownNow();
        logger.info("heartbeat executor shutdown");

        registryClient.close();
        logger.info("registry client closed");
    }

    /**
     * get server path
     */
    public String getServerZkPath(String serverName, String address) {
        StringJoiner workerPathJoiner = new StringJoiner(SINGLE_SLASH);
        workerPathJoiner.add(REGISTRY_DP_NETTY);
        workerPathJoiner.add(serverName.trim().toLowerCase());
        workerPathJoiner.add(address);

        return workerPathJoiner.toString();
    }

    public void handleDeadServer(Set<String> nodeSet, NodeType nodeType, String opType) {
        registryClient.handleDeadServer(nodeSet, nodeType, opType);
    }

    /**
     * get local address
     */
    private String getLocalAddress() {
        return NetUtils.getAddr(this.listenPort);
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    public boolean checkNodeExists(NodeType nodeType) {
        boolean result = registryClient.checkNodeExists(NetUtils.getHost(), nodeType);
        if (result) {
            logger.info("check severName:{}, node exist success, host:{}", nodeType.getServerName(), NetUtils.getHost());
        }
        return result;
    }
}
