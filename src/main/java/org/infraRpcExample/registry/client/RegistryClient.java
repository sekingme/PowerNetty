 /* power by sekingme */

package org.infraRpcExample.registry.client;

import com.google.common.base.Strings;
import org.infraRpcExample.common.Server;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.exceptions.RegistryException;
import org.infraRpcExample.registry.*;
import org.infraRpcExample.registry.task.HeartBeat;
import org.infraRpcExample.thread.IStoppable;
import org.infraRpcExample.utils.Constants;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.infraRpcExample.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static java.awt.font.TextAttribute.UNDERLINE;
import static org.apache.commons.collections4.CollectionUtils.COLON;
import static org.infraRpcExample.utils.Constants.*;

/**
 * @author: sekingme
 * @description: 注册中心的统一客户端
 * @create: 2024-10-08 15:41
 */
@Component
public class RegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    private static final String        EMPTY           = "";
    private static final String        REGISTRY_PREFIX = "registry";
    private final        AtomicBoolean isStarted       = new AtomicBoolean(false);
    private              Registry      registry;
    private              IStoppable    stoppable;

    @Value("${registry.plugin.name:zookeeper}")
    private String registryPluginName;

    @PostConstruct
    public void afterConstruct() {
        start();
        initNodes();
    }


    /**
     * TODO：返回server结构，待明确如何使用
     *
     * @param nodeType
     * @return
     */
    public List<Server> getServerNodeList(NodeType nodeType) throws Exception {
        Map<String, String> serverMaps = getSingleServerInfoMaps(nodeType);
        String              parentPath = rootNodePath(nodeType);

        List<Server> serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverMaps.entrySet()) {
            HeartBeat heartBeat = HeartBeat.decodeHeartBeat(entry.getValue());
            if (heartBeat == null) {
                continue;
            }

            Server server = new Server();
            server.setResInfo(ObjectMapperConfig.toCommonJsonString(heartBeat));
            server.setCreateTime(new Date(heartBeat.getStartupTime()));
            server.setLastHeartbeatTime(new Date(heartBeat.getReportTime()));
            server.setId(heartBeat.getProcessId());

            String key = entry.getKey();
            server.setZkDirectory(parentPath + "/" + key);
            // set host and port
            String[] hostAndPort = key.split(COLON);
            String[] hosts       = hostAndPort[0].split(DIVISION_STRING);
            // fetch the last one
            server.setHost(hosts[hosts.length - 1]);
            server.setPort(Integer.parseInt(hostAndPort[1]));
            serverList.add(server);
        }
        return serverList;
    }

    public Map<String, String> getSingleServerInfoMaps(NodeType nodeType) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            String             path       = rootNodePath(nodeType);
            Collection<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                serverMap.putIfAbsent(server, get(path + SINGLE_SLASH + server));
            }
        } catch (Exception e) {
            logger.error("get server info map failed", e);
        }

        return serverMap;
    }

    public Map<String, String> getAllServerInfoMaps() {
        Map<String, String> serverMap = new HashMap<>();
        try {
            for (NodeType nodeType : NodeType.values()) {
                String             path       = rootNodePath(nodeType);
                Collection<String> serverList = getServerNodes(nodeType);
                for (String server : serverList) {
                    serverMap.putIfAbsent(server, get(path + SINGLE_SLASH + server));
                }
            }
        } catch (Exception e) {
            logger.error("get all server info map failed", e);
        }

        return serverMap;
    }

    public boolean checkNodeExists(String host, NodeType nodeType) {
        return getServerNodes(nodeType).stream().anyMatch(it -> it.contains(host));
    }

    public void handleDeadServer(Collection<String> nodes, NodeType nodeType, String opType) {
        nodes.forEach(node -> {
            final String host           = getHostByEventDataPath(node);
            String       deadServerPath = REGISTRY_DP_DEAD_SERVERS + SINGLE_SLASH + nodeType.getServerName() + UNDERLINE + host;

            if (opType.equals(DELETE_OP)) {
                remove(deadServerPath);
                logger.info("{} server {} deleted from zk dead server path:{} success", nodeType.getServerName(), host, deadServerPath);
            } else if (opType.equals(ADD_OP)) {
                // Add dead server info to zk dead server path : /dead-servers/
                registry.put(deadServerPath, nodeType.getServerName() + UNDERLINE + host, false);
                logger.info("{} server {} dead. And {} added to zk dead server path success", nodeType.getServerName(), host, node);
            }
        });
    }

    public boolean checkIsDeadServer(String node, String serverType) {
        // ip_sequence_no
        String[] zNodesPath     = node.split(SINGLE_SLASH);
        String   ipSeqNo        = zNodesPath[zNodesPath.length - 1];
        String   deadServerPath = REGISTRY_DP_DEAD_SERVERS + SINGLE_SLASH + serverType + UNDERLINE + ipSeqNo;

        return !exists(node) || exists(deadServerPath);
    }

    public Collection<String> getServerDirectly() {
        return getChildrenKeys(REGISTRY_DP_NETTY);
    }

    public Collection<String> getServerNodesDirectly(String serverName) {
        return getChildrenKeys(REGISTRY_DP_NETTY + Constants.SINGLE_SLASH + serverName);
    }

    /**
     * get host ip:port, path format: parentPath/ip:port
     *
     * @param path path
     * @return host ip:port, string format: parentPath/ip:port
     */
    public String getHostByEventDataPath(String path) {
        checkArgument(!Strings.isNullOrEmpty(path), "path cannot be null or empty");

        final String[] pathArray = path.split(SINGLE_SLASH);

        checkArgument(pathArray.length >= 1, "cannot parse path: %s", path);

        return pathArray[pathArray.length - 1];
    }

    public void close() throws IOException {
        if (isStarted.compareAndSet(true, false) && registry != null) {
            registry.close();
        }
    }

    public void persistEphemeral(String key, String value) {
        registry.put(key, value, true);
    }

    public void remove(String key) {
        registry.delete(key);
    }

    public String get(String key) {
        return registry.get(key);
    }

    public void subscribe(String path, SubscribeListener listener) {
        registry.subscribe(path, listener);
    }

    public void addConnectionStateListener(ConnectionListener listener) {
        registry.addConnectionStateListener(listener);
    }

    public boolean exists(String key) {
        return registry.exists(key);
    }

    public boolean getLock(String key) {
        return registry.acquireLock(key);
    }

    public boolean releaseLock(String key) {
        return registry.releaseLock(key);
    }

    public IStoppable getStoppable() {
        return stoppable;
    }

    public void setStoppable(IStoppable stoppable) {
        this.stoppable = stoppable;
    }

    public Collection<String> getChildrenKeys(final String key) {
        return registry.children(key);
    }

    private void start() {
        if (isStarted.compareAndSet(false, true)) {
            final Map<String, String> registryConfig = PropertyUtils.getPropertiesByPrefix(REGISTRY_PREFIX);
//            final Map<String, String> registryConfig = PropertyUtils.getConfigsStartingWith(REGISTRY_PREFIX);// todo: 生产环境改为apollo配置中心获取

            if (null == registryConfig || registryConfig.isEmpty()) {
                throw new RegistryException("registry config param is null");
            }
            final Map<String, RegistryFactory> factories = RegistryFactoryLoader.load();
            if (!factories.containsKey(registryPluginName)) {
                throw new RegistryException("No such registry plugin: " + registryPluginName);
            }
            registry = factories.get(registryPluginName).create();
            registry.start(registryConfig);
        }
    }

    private void initNodes() {
        registry.put(REGISTRY_DP_NETTY, EMPTY, false);
        registry.put(REGISTRY_DP_DEAD_SERVERS, EMPTY, false);
    }

    private String rootNodePath(NodeType type) {
        return REGISTRY_DP_NETTY + SINGLE_SLASH + type.getServerName();
    }

    private Collection<String> getServerNodes(NodeType nodeType) {
        final String path = rootNodePath(nodeType);
        return getChildrenKeys(path);
    }
}
