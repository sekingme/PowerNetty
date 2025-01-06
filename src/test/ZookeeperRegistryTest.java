package org.infraRpcExample.registry.impl.test;

import org.apache.curator.test.TestingServer;
import org.infraRpcExample.registry.Event;
import org.infraRpcExample.registry.SubscribeListener;
import org.infraRpcExample.registry.impl.zookeeper.ZookeeperConfiguration;
import org.infraRpcExample.registry.impl.zookeeper.ZookeeperRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Sekingme
 * @description:
 * @create: 2024-10-09 15:30
 **/
public class ZookeeperRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryTest.class);

    TestingServer server;

    ZookeeperRegistry registry = new ZookeeperRegistry();

    @Before
    public void before() throws Exception {
        server = new TestingServer(true);
        Map<String, String> registryConfig = new HashMap<>();
        registryConfig.put(ZookeeperConfiguration.SERVERS.getName(), "10.190.48.8:2181,10.190.48.9:2181,10.190.48.10:2181");
        registry.start(registryConfig);
        registry.put("/sub", "", false);
    }

    @Test
    public void persistTest() {
        registry.put("/nodes/m1", "", false);
        registry.put("/nodes/m2", "", false);
        Assert.assertEquals(Arrays.asList("m2", "m1"), registry.children("/nodes"));
        Assert.assertTrue(registry.exists("/nodes/m1"));
        registry.delete("/nodes/m2");
        Assert.assertFalse(registry.exists("/nodes/m2"));
    }

    @Test
    public void subscribeTest() {
        boolean status = registry.subscribe("/sub", new TestListener());
        Assert.assertTrue(status);

    }

    static class TestListener implements SubscribeListener {
        @Override
        public void notify(Event event) {
            logger.info("I'm test listener");
        }
    }

    @After
    public void after() throws IOException {
        registry.close();
        server.close();
    }

}
