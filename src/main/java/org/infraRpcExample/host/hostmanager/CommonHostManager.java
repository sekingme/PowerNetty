 /* power by sekingme */

package org.infraRpcExample.host.hostmanager;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.host.HostWeight;
import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.host.hostmanager.interfaces.HostManager;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.registry.manager.ServerNodeManager;
import org.infraRpcExample.registry.task.HeartBeat;
import org.infraRpcExample.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * common host manager
 * 负载均衡算法管理器，抽象类
 *
 * @author sekingme
 */
public abstract class CommonHostManager implements HostManager {

    private final Logger logger = LoggerFactory.getLogger(CommonHostManager.class);

    @Autowired
    protected ServerNodeManager serverNodeManager;

    /**
     * select host
     *
     * @param rpcRequest rpcRequest
     * @return host
     */
    @Override
    public Host select(RpcRequest rpcRequest) {
        Set<HostWorker> candidates = getWorkerCandidates(rpcRequest.getNodeType());

        if (CollectionUtils.isEmpty(candidates)) {
            logger.error("None candidates sever node can be found for server {}", rpcRequest.getNodeType().getServerName());
            return new Host();
        }

        if (candidates.size() == 1) {
            return candidates.iterator().next();
        }
        return select(candidates, rpcRequest);
    }

    protected abstract HostWorker select(Collection<HostWorker> nodes, RpcRequest request);

    protected Set<HostWorker> getWorkerCandidates(NodeType nodeType) {
        Set<HostWorker> workers = new HashSet<>();
        try {
            Set<String> nodes = serverNodeManager.getSingleServerNodes(nodeType.getServerName());
            for (String node : nodes) {
                String               heartbeat     = serverNodeManager.getServerNodeInfo(node);
                Optional<HostWorker> hostWorkerOpt = getHostWorker(node, nodeType, heartbeat);
                hostWorkerOpt.ifPresent(workers::add);
            }
            return workers;
        } catch (Throwable ex) {
            logger.error("RefreshResourceTask error", ex);
        }
        return workers;
    }

    private Optional<HostWorker> getHostWorker(String addr, NodeType nodeType, String heartBeatInfo) {
        if (StringUtils.isEmpty(heartBeatInfo)) {
            logger.warn("server {} in {} have not received the heartbeat", nodeType.getServerName(), addr);
            return Optional.empty();
        }
        HeartBeat heartBeat = HeartBeat.decodeHeartBeat(heartBeatInfo);
        if (heartBeat == null) {
            return Optional.empty();
        }
        switch (heartBeat.getServerStatus()) {
            case Constants.ABNORMAL_NODE_STATUS:
                logger.warn("server node {} current cpu load average {} is too high or available memory {}G is too low",
                        addr, heartBeat.getLoadAverage(), heartBeat.getAvailablePhysicalMemorySize());
                return Optional.empty();
            case Constants.BUSY_NODE_STATUS:
                logger.warn("server node {} is busy, current waiting task count {} is large than worker thread count {}",
                        addr, heartBeat.getWorkerWaitingTaskCount(), heartBeat.getWorkerExecThreadCount());
                return Optional.empty();
            default:
                return Optional.of(
                        HostWorker.of(addr, new HostWeight(heartBeat.getCpuUsage(), heartBeat.getMemoryUsage(), heartBeat.getLoadAverage(),
                                heartBeat.getWorkerWaitingTaskCount(), heartBeat.getStartupTime()), nodeType));
        }
    }


}
