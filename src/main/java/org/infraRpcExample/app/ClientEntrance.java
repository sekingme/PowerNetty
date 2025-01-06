package org.infraRpcExample.app;

import org.apache.commons.lang3.ClassUtils;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.protocol.RpcResponse;
import org.infraRpcExample.remote.NettyClient;
import org.infraRpcExample.service.ReportService;
import org.infraRpcExample.service.UserDataService;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sekingme
 */
public class ClientEntrance {

    // 多进程通信真实客户端
    public static void main(String[] args) {
        try {
            Host host = new Host("172.19.88.131", 8085);

            ExecutorService executorService = Executors.newFixedThreadPool(55);

            // 大量并发
            for (int i = 0; i <= 25; i++) {
//                Thread.sleep(500L);
                int finalI = i;
                executorService.submit(() -> {
                    try {
                        Method method = ClassUtils.getPublicMethod(ReportService.class, "getReport", String.class, int.class);
                        RpcRequest request = RpcRequest.builder()
                                .nodeType(NodeType.NETTY_TEST)
                                .serviceName(method.getDeclaringClass().getName())
                                .methodName(method.getName())
                                .parameterTypes(method.getParameterTypes())
                                .parameters(new Object[]{"Say you love me JSQ!" + finalI, 123123123})
                                .build();
                        // 模拟任务执行
                        runTask(request, host);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Task completed.");
                });
            }

            // 单独提交任务
            executorService.submit(() -> {
                try {
                    Method method = ClassUtils.getPublicMethod(UserDataService.class, "getUserData", String.class);
                    RpcRequest request = RpcRequest.builder()
                            .nodeType(NodeType.NETTY_TEST)
                            .serviceName(method.getDeclaringClass().getName())
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(new Object[]{"Say I love you JSQ!"})
                            .build();
                    // 模拟任务执行
                    runTask(request, host);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task completed.");
                System.out.println("\n");
            });

            // 单独提交另一个任务
            executorService.submit(() -> {
                try {
                    Method method = ClassUtils.getPublicMethod(ReportService.class, "getReport", String.class, int.class);
                    RpcRequest request = RpcRequest.builder()
                            .nodeType(NodeType.NETTY_TEST)
                            .serviceName(method.getDeclaringClass().getName())
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(new Object[]{"Say you love me JSQ!", 123123123})
                            .build();
                    // 模拟任务执行
                    runTask(request, host);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task completed.");
            });

            Thread.sleep(1000L);
            NettyClient.getInstance().getMap().forEach((key, value) -> System.out.println(key + "::" + value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTask(RpcRequest request, Host host) throws Exception {
        // 发送请求并接收响应
        RpcResponse<?> response = null;
        try {
            response = NettyClient.getInstance().sendBatchSync(request, host, 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response.getError() == null) {
            System.out.println("Result: " + response.getResult());
        } else {
            System.out.println(response.getError());
        }
    }
}
