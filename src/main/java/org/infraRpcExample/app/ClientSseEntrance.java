package org.infraRpcExample.app;

import org.apache.commons.lang3.ClassUtils;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.remote.NettyClient;
import org.infraRpcExample.request.PowerSqlRequest;
import org.infraRpcExample.service.NatureChatService;
import org.infraRpcExample.utils.ObjectMapperConfig;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author zhushuhao
 * <p>
 * 启动SpringEntranceTest 后，再运行ClientSseEntrance即可测试rpc形式的流式问答
 */
public class ClientSseEntrance {

    // 多进程通信真实客户端
    public static void main(String[] args) {
        try {
            String          body        = "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\",\"externInfo\":{\"files\":[]}}],\"stream\":true,\"temperature\":0.8,\"agentId\":1,\"model\":\"gpt-4o\",\"chatId\":\"1e88724f-9583-422f-91b6-34a2f7b2b750\",\"username\":\"zhushuhao\",\"userWorkId\":\"08715\"}";
            String          requestBody = String.format(body, "hello");
            PowerSqlRequest chatRequest = ObjectMapperConfig.parseCommonObject(requestBody, PowerSqlRequest.class);

            Host   host   = new Host("localhost", 8085);
            Method method = ClassUtils.getPublicMethod(NatureChatService.class, "natureChatEmitter", PowerSqlRequest.class);
            RpcRequest rpcRequest = RpcRequest.builder()
                    .nodeType(NodeType.NETTY_TEST)
                    .serviceName(method.getDeclaringClass().getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .parameters(new Object[]{chatRequest})
                    .build();

            new Thread(() -> {
                NettyClient instance = NettyClient.getInstance();

                CompletableFuture<Void> completableFuture = null;
                try {
                    completableFuture = instance.sendSseSync(rpcRequest, host, 100 * 1000L, data -> {
                        System.out.println("First Received data: " + data);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                completableFuture.thenRun(() -> {
                    System.out.println("Connection closed.");
                    instance.close();
                }).exceptionally(e -> {
                    e.printStackTrace();
                    instance.close();
                    return null;
                });
            }).start();


            requestBody = String.format(body, "hi");
            chatRequest = ObjectMapperConfig.parseCommonObject(requestBody, PowerSqlRequest.class);
            rpcRequest.setParameters(new Object[]{chatRequest});

            new Thread(() -> {
                NettyClient instance = NettyClient.getInstance();

                CompletableFuture<Void> completableFuture = null;
                try {
                    completableFuture = instance.sendSseSync(rpcRequest, host, 100 * 1000L, data -> {
                        System.out.println("Second Received data: " + data);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                completableFuture.thenRun(() -> {
                    System.out.println("Connection closed.");
                    instance.close();
                }).exceptionally(e -> {
                    e.printStackTrace();
                    instance.close();
                    return null;
                });
            }).start();


            requestBody = String.format(body, "你好");
            chatRequest = ObjectMapperConfig.parseCommonObject(requestBody, PowerSqlRequest.class);
            rpcRequest.setParameters(new Object[]{chatRequest});
            new Thread(() -> {
                NettyClient instance = NettyClient.getInstance();

                CompletableFuture<Void> completableFuture = null;
                try {
                    completableFuture = instance.sendSseSync(rpcRequest, host, 100 * 1000L, data -> {
                        System.out.println("Third Received data: " + data);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                completableFuture.thenRun(() -> {
                    System.out.println("Connection closed.");
                    instance.close();
                }).exceptionally(e -> {
                    e.printStackTrace();
                    instance.close();
                    return null;
                });
            }).start();

            new Thread(() -> {
                NettyClient instance = NettyClient.getInstance();
                try {
                    // 30s后关闭NettyClient
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance.close();
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
