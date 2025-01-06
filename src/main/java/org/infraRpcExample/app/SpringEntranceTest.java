package org.infraRpcExample.app;

import org.infraRpcExample.config.NettyServerConfig;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.handler.scanner.ServiceScanner;
import org.infraRpcExample.registry.client.service.ServerRegistryClient;
import org.infraRpcExample.remote.NettyServer;
import org.infraRpcExample.thread.IStoppable;
import org.infraRpcExample.thread.Stopper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * @author: sekingme
 * @description:
 * @create: 2024-09-25 15:04
 **/

@SpringBootApplication
@ComponentScan("org.infraRpcExample")
public class SpringEntranceTest implements IStoppable {

    private final Logger logger = LoggerFactory.getLogger(SpringEntranceTest.class);

    @Autowired
    ServerRegistryClient serverRegistryClient;

    NettyServer nettyServer = null;

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        Thread.currentThread().setName("netty_test");
        SpringApplication.run(SpringEntranceTest.class, args);

        ServiceScanner.getServiceMap().forEach((key, value) -> System.out.println(key + ":" + value));

//        // 测试包下各个类是否成功被扫描注入bean
//        for (String name : context.getBeanNamesForType(CommandLineRunner.class)) {
//            System.out.println("Found CommandLineRunner: " + name);
//        }
    }

    @PostConstruct
    public void run() {
        NettyServerConfig nettyServerConfig = NettyServerConfig.builder()
//                .listenPort(OSUtils.findAvailablePort()) todo: 生产环境中要打开这个注释，避免端口可能被占用导致启动失败
                .build();

        try {
            nettyServer = new NettyServer(nettyServerConfig);
            nettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            serverRegistryClient.registry(NodeType.NETTY_TEST, nettyServerConfig.getListenPort());
            serverRegistryClient.setRegistryStoppable(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * registry hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Stopper.isRunning()) {
                close("shutdownHook");
            }
        }));
    }

    public void close(String cause) {
        try {
            // execute only once
            if (Stopper.isStopped()) {
                return;
            }

            logger.warn("server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            try {
                // thread sleep 1 seconds for thread quietly stop
                Thread.sleep(1000L);
            } catch (Exception e) {
                logger.warn("thread sleep exception", e);
            }

            // close
            this.nettyServer.close();
            this.serverRegistryClient.unRegistry();

            try {
                // thread sleep 5 seconds for quietly stop
                Thread.sleep(5000L);
            } catch (Exception e) {
                logger.warn("thread sleep exception ", e);
            }
            Runtime.getRuntime().halt(0);
        } catch (Exception e) {
            logger.error("server stop exception ", e);
            Runtime.getRuntime().halt(1);
        }
    }

    @Override
    public void stop(String cause) {
        close(cause);
    }
}