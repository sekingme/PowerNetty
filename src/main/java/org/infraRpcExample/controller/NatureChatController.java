package org.infraRpcExample.controller;


import org.apache.commons.lang3.ClassUtils;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.listener.SseEmitterDataConsumer;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.remote.NettyClient;
import org.infraRpcExample.request.PowerSqlRequest;
import org.infraRpcExample.service.NatureChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lingyu Zhang
 * Time: 2024/5/30 3:07 下午
 **/


@RestController
@RequestMapping("chat/natureChat")
public class NatureChatController {

    private final Logger logger = LoggerFactory.getLogger(NatureChatController.class);

    @PostMapping(value = "/chatStream", headers = "Accept=*/*", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter natureChat(@RequestBody PowerSqlRequest chatRequest) {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        try {
            Host   host   = new Host("localhost", 8085);
            Method method = ClassUtils.getPublicMethod(NatureChatService.class, "natureChatEmitter", PowerSqlRequest.class);
            RpcRequest rpcRequest = RpcRequest.builder()
                    .nodeType(NodeType.NETTY_TEST)
                    .serviceName(method.getDeclaringClass().getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .parameters(new Object[]{chatRequest})
                    .build();

            NettyClient instance = NettyClient.getInstance();

            CompletableFuture<Void> completableFuture = null;
            try {
                completableFuture = instance.sendSseSync(rpcRequest, host, 100 * 1000L,
                        new SseEmitterDataConsumer(sseEmitter));
            } catch (Exception e) {
                logger.error("Exception:", e);
            }

            completableFuture.thenRun(() -> {
                logger.info("Connection closed.");
                instance.close();
            }).exceptionally(e -> {
                logger.info("Connection closed because Exception.", e);
                instance.close();
                return null;
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return sseEmitter;
    }
}
