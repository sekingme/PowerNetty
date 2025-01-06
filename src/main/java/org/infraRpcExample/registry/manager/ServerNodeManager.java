 /* power by sekingme */

package org.infraRpcExample.registry.manager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.registry.Event;
import org.infraRpcExample.registry.SubscribeListener;
import org.infraRpcExample.registry.client.RegistryClient;
import org.infraRpcExample.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.infraRpcExample.utils.Constants.REGISTRY_DP_NETTY;

/**
 * @author: sekingme
 * @description: 用于监听各服务节点及信息变化，以及缓存各服务在注册中心的数据
 * @create: 2024-09-25 17:34
 */
@Service
public class ServerNodeManager implements InitializingBean {

    /**
     * eg : /datapower/netty/servers/vis/127.0.0.1:xxx
     */
    private static final int SERVER_LISTENER_CHECK_LENGTH = 6;
    private final Logger logger = LoggerFactory.getLogger(ServerNodeManager.class);
    /**
     * server lock
     */
    private final Lock serverLock = new ReentrantLock();
    /**
     * server info lock
     */
    private final Lock serverInfoLock = new ReentrantLock();
    /**
     * server nodes
     */
    private final ConcurrentHashMap<String, Set<String>> serverNodes = new ConcurrentHashMap<>();
    /**
     * server node info
     */
    private final Map<String, String> severNodeInfo = new HashMap<>();
    @Autowired
    private RegistryClient registryClient;

    /**
     * init listener
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * load nodes from zookeeper
         */
        load();

        /**
         * 初始化各个服务的listener
         */
        for (NodeType nodeType : NodeType.values()) {
            registryClient.subscribe(REGISTRY_DP_NETTY + Constants.SINGLE_SLASH + nodeType.getServerName(),
                    new ServerDataListener(nodeType));
        }
    }

    /**
     * load nodes from zookeeper
     */
    public void load() {
        /*
         * server nodes from zookeeper
         */
        Collection<String> servers = registryClient.getServerDirectly();
        for (String server : servers) {
            syncServerNodes(server, registryClient.getServerNodesDirectly(server));
        }

        // sync all server node info
        Map<String, String> newServerNodeInfo = registryClient.getAllServerInfoMaps();
        syncAllServerNodeInfo(newServerNodeInfo);
    }

    /**
     * remove server node path
     *
     * @param path     node path
     * @param nodeType node type
     */
    public void removeServerNodePath(String path, NodeType nodeType) {
        logger.info("{} node deleted : {}", nodeType.getServerName(), path);
        try {
            String serverHost = null;
            if (!StringUtils.isEmpty(path)) {
                serverHost = registryClient.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    logger.error("server down error: unknown path: {}", path);
                    return;
                }
                if (!registryClient.exists(path)) {
                    logger.info("path: {} not exists", path);
                    // handle dead server
                    registryClient.handleDeadServer(Collections.singleton(path), nodeType, Constants.ADD_OP);
                }
            }
        } catch (Exception e) {
            logger.error("removeServerNodePath {} of server {} failed", path, nodeType.getServerName(), e);
        }
    }

    /**
     * sync Server group nodes
     *
     * @param serverName serverName
     * @param nodes      server nodes
     */
    private void syncServerNodes(String serverName, Collection<String> nodes) {
        serverLock.lock();
        try {
            Set<String> sNodes = serverNodes.getOrDefault(serverName, new HashSet<>());
            sNodes.clear();
            sNodes.addAll(nodes);
            serverNodes.put(serverName, sNodes);
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * get server nodes
     *
     * @param serverName nodeType
     * @return server nodes
     */
    public Set<String> getSingleServerNodes(String serverName) {
        serverLock.lock();
        try {
            if (StringUtils.isEmpty(serverName)) {
                logger.error("Must set serverName while getting server nodes.");
            }
            Set<String> nodes = serverNodes.get(serverName);
            if (CollectionUtils.isNotEmpty(nodes)) {
                // avoid ConcurrentModificationException
                return Collections.unmodifiableSet(nodes.stream().collect(Collectors.toSet()));
            }
            return nodes;
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * sync single server node info
     */
    private void syncSingleServerNodeInfo(String node, String info) {
        serverInfoLock.lock();
        try {
            severNodeInfo.put(node, info);
        } finally {
            serverInfoLock.unlock();
        }
    }

    public Map<String, Set<String>> getAllServerNodes() {
        serverLock.lock();
        try {
            return Collections.unmodifiableMap(serverNodes);
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * sync server node info
     * eg: ["127.0.0.0:80": "encode[heatbeat]"]
     *
     * @param newServerNodeInfo new server node info
     */
    private void syncAllServerNodeInfo(Map<String, String> newServerNodeInfo) {
        serverInfoLock.lock();
        try {
            severNodeInfo.clear();
            severNodeInfo.putAll(newServerNodeInfo);
        } finally {
            serverInfoLock.unlock();
        }
    }

    /**
     * get server node info
     * 例如: serverNode: "127.0.0.0:80"
     * 使用：比如最低负载均衡算法，需要获取节点的信息，value就是节点信息
     *
     * @param serverNode server node
     * @return server node info
     */
    public String getServerNodeInfo(String serverNode) {
        serverInfoLock.lock();
        try {
            return severNodeInfo.getOrDefault(serverNode, null);
        } finally {
            serverInfoLock.unlock();
        }
    }

    /**
     * server node listener
     */
    class ServerDataListener implements SubscribeListener {

        NodeType nodeType;

        public ServerDataListener(NodeType nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public void notify(Event event) {
            final String     path = event.path();
            final Event.Type type = event.type();
            final String     data = event.data();
            try {
                if (type == Event.Type.ADD) {
                    logger.info("server node : {} added.", path);
                    Collection<String> currentNodes = registryClient.getServerNodesDirectly(nodeType.getServerName());
                    logger.info("Current nodes of server {} is: {}", nodeType.getServerName(), currentNodes);
                    syncServerNodes(nodeType.getServerName(), currentNodes);
                } else if (type == Event.Type.REMOVE) {
                    logger.info("server node : {} down.", path);
                    removeServerNodePath(path, nodeType);
                    Collection<String> currentNodes = registryClient.getServerNodesDirectly(nodeType.getServerName());
                    logger.info("Current nodes of server {} is: {}", nodeType.getServerName(), currentNodes);
                    syncServerNodes(nodeType.getServerName(), currentNodes);
                    // todo: 发个企微告警
                } else if (type == Event.Type.UPDATE) {
                    logger.debug("server node : {} update, data: {}", path, data);
                    Collection<String> currentNodes = registryClient.getServerNodesDirectly(nodeType.getServerName());
                    logger.info("Current nodes of server {} is: {}", nodeType.getServerName(), currentNodes);
                    syncServerNodes(nodeType.getServerName(), currentNodes);

                    String node = parseNode(path);
                    syncSingleServerNodeInfo(node, data);
                }
            } catch (IllegalArgumentException ex) {
                logger.warn(ex.getMessage());
            } catch (Exception ex) {
                logger.error("ServerNodeListener capture data change and get data failed", ex);
            }
        }

        private String parseNode(String path) {
            String[] parts = path.split(Constants.SINGLE_SLASH);
            if (parts.length < SERVER_LISTENER_CHECK_LENGTH) {
                throw new IllegalArgumentException(String.format("server node path : %s is not valid, ignore", path));
            }
            return parts[parts.length - 1];
        }
    }

}
