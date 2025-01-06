package org.infraRpcExample.host;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.infraRpcExample.app.SpringEntranceTest;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.hostmanager.ConsistentHashHostManager;
import org.infraRpcExample.host.hostmanager.LowerWaitingTaskHostManager;
import org.infraRpcExample.host.hostmanager.RandomHostManager;
import org.infraRpcExample.host.hostmanager.RoundRobinHostManager;
import org.infraRpcExample.host.hostmanager.interfaces.HostManager;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.registry.task.HeartBeat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Yuanda Liao
 * Time:   2024/9/30 11:28
 */
@Slf4j
@SpringBootTest(classes = {SpringEntranceTest.class})
@RunWith(SpringRunner.class)
public class HostManagerTest {

    @Autowired
    private RandomHostManager randomHostManager;

//    @Autowired
    private ConsistentHashHostManager consistentHashHostManager;

//    @Autowired
    private LowerWaitingTaskHostManager lowerWaitingTaskHostManager;

//    @Autowired
    private RoundRobinHostManager roundRobinHostManager;

    private HostManager getHostManager() {
        return randomHostManager;
    }

    private int index = 0;

    @Test
    public void testOneRequest() {
        RpcRequest rpcRequest = RpcRequest.builder().nodeType(NodeType.NETTY_TEST)
                .serviceName("testService")
                .methodName("testOneRequest")
                .parameters(new ArrayList[]{Lists.newArrayList("p1", "p2", "p3")})
                .build();

        HostManager hostManager = getHostManager();
        Host        host        = hostManager.select(rpcRequest);
        System.out.println(host);
    }

    @Test
    public void testCurrentRequest() {
        Lock                      lock         = new ReentrantLock();
        Map<String, Integer>      nodeCountMap = Maps.newHashMap();
        List<FutureTask<Boolean>> futureTasks  = Lists.newArrayList();
        List<List<Object>>        plists       = Lists.newArrayList(Lists.newArrayList("p1"), Lists.newArrayList("p2"), Lists.newArrayList("p1", "p2"));
        int                       batchSize    = 1200;
        for (int i = 0; i < batchSize; i++) {
            FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
                RpcRequest rpcRequest = RpcRequest.builder().nodeType(NodeType.NETTY_TEST)
                        .serviceName("testService")
                        .methodName("testCurrentRequest")
                        .parameters(new List[]{plists.get(ThreadLocalRandom.current().nextInt(plists.size()))})
                        .build();

                HostManager hostManager = getHostManager();
                Host        host        = hostManager.select(rpcRequest);
                lock.lock();
                try {
                    nodeCountMap.put(host.getAddress(), nodeCountMap.getOrDefault(host.getAddress(), 0) + 1);
                } finally {
                    lock.unlock();
                }
                System.out.println(rpcRequest + " --> " + host);
                return true;
            });
            new Thread(futureTask).start();
            futureTasks.add(futureTask);
        }
        for (FutureTask<Boolean> futureTask : futureTasks) {
            try {
                futureTask.get();
            } catch (Exception e) {
                log.error("error", e);
            }
        }
        System.out.println(nodeCountMap);
    }

    @Test
    public void testLoopRequest() throws InterruptedException {
        List<List<Object>> plists = Lists.newArrayList(Lists.newArrayList("p1"), Lists.newArrayList("p2"), Lists.newArrayList("p1", "p2"));
        for (int j = 0; j < 10; j++) {
            Map<String, Integer> nodeCountMap = Maps.newHashMap();
            System.out.println("loop " + j);
            int batchSize = 300;
            for (int i = 0; i < batchSize; i++) {
                RpcRequest rpcRequest = RpcRequest.builder().nodeType(NodeType.NETTY_TEST)
                        .serviceName("testService")
                        .methodName("testLoopRequest")
                        .parameters(new List[]{plists.get(ThreadLocalRandom.current().nextInt(plists.size()))})
                        .build();
                HostManager hostManager = getHostManager();
                Host        host        = hostManager.select(rpcRequest);
                nodeCountMap.put(host.getAddress(), nodeCountMap.getOrDefault(host.getAddress(), 0) + 1);
                System.out.println(rpcRequest + " --> " + host);
            }
            System.out.println(nodeCountMap);
            TimeUnit.SECONDS.sleep(3);
        }
    }

    @Test
    public void test() throws Exception {
        for (String node : getServerNodes().get(NodeType.NETTY_TEST.getServerName())) {
            System.out.println(node + "   " + getServerNodeInfo(node));
            System.out.println(new ObjectMapper().writeValueAsString(HeartBeat.decodeHeartBeat(getServerNodeInfo(node))));
        }
    }

    private Map<String, Set<String>> getServerNodes() {
        Map<String, Set<String>> serverNodes = Maps.newHashMap();
        serverNodes.put(NodeType.NETTY_TEST.getServerName(),
                Sets.newHashSet("127.0.0.1:7000", "127.0.0.2:7000", "127.0.0.3:7000"));
        return serverNodes;
    }

    private String getServerNodeInfo(String node) {
        Map<String, String> nodeInfosMap = Maps.newHashMap();
        long                startTime0   = System.currentTimeMillis() - 20 * 60 * 1000;
        long                startTime1   = System.currentTimeMillis() - 20 * 60 * 1000;
        long                startTime2   = System.currentTimeMillis() - 20 * 60 * 1000;
        // cpu,mem,avaDisk,load,avaMem,,,,waitTaskCount
        List<List<String>> nodeInfos = Lists.newArrayList(
                Lists.newArrayList(
                        "0.10,0.18,500,0.15,60,0.5,0.5," + startTime0 + "," + System.currentTimeMillis() + ",0,351111,100,1000,200",
                        "0.30,0.18,500,0.20,60,0.5,0.5," + startTime1 + "," + System.currentTimeMillis() + ",0,351222,100,1000,200",
                        "0.10,0.10,500,0.50,60,0.5,0.5," + startTime2 + "," + System.currentTimeMillis() + ",0,351333,100,1000,200"
                ),
                Lists.newArrayList(
                        "0.10,0.18,500,0.15,60,0.5,0.5," + startTime0 + "," + System.currentTimeMillis() + ",0,351111,100,1000,0",
                        "0.30,0.18,500,0.20,60,0.5,0.5," + startTime1 + "," + System.currentTimeMillis() + ",0,351222,100,1000,200",
                        "0.70,0.30,500,0.50,60,0.5,0.5," + startTime2 + "," + System.currentTimeMillis() + ",0,351333,100,1000,200"
                )
        );
        index = 1 - index;
        List<String> nodeInfoList = nodeInfos.get(index);
        List<String> nodes        = getServerNodes().get(NodeType.NETTY_TEST.getServerName()).stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < nodes.size(); i++) {
            nodeInfosMap.put(nodes.get(i), nodeInfoList.get(i));
        }
        return nodeInfosMap.get(node);
    }
}
