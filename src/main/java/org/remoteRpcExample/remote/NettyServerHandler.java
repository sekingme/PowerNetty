package org.remoteRpcExample.remote;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.remoteRpcExample.RpcProtocol.EventType;
import org.remoteRpcExample.RpcProtocol.RpcProtocol;
import org.remoteRpcExample.common.ThreadPoolManager;
import org.remoteRpcExample.dto.RpcRequest;
import org.remoteRpcExample.dto.RpcResponse;
import org.remoteRpcExample.service.impl.ReportServiceImpl;
import org.remoteRpcExample.service.impl.UserDataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Sekingme
 * @description:
 * @create: 2024-09-10 14:52
 **/
class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final ThreadPoolManager THREAD_POOL_MANAGER = ThreadPoolManager.INSTANCE;

    // 注册服务实例
    private static final Map<String, Object> SERVICE_REGISTRY = new HashMap<>();

    static {
        // 注册服务实现类实例
        SERVICE_REGISTRY.put("UserDataService", new UserDataServiceImpl());
        SERVICE_REGISTRY.put("ReportService", new ReportServiceImpl());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("channel close");
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("client connect success !" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcProtocol<RpcRequest> rpcProtocol = (RpcProtocol<RpcRequest>) msg;
        THREAD_POOL_MANAGER.addExecuteTask(() -> handleMsg(ctx, rpcProtocol));
    }

    private void handleMsg(ChannelHandlerContext ctx, RpcProtocol protocol) {
        RpcResponse<Object> response = new RpcResponse<>();
        RpcRequest request = (RpcRequest) protocol.getBody();

        // 查找服务实例 todo: 可以通过spring实现ServiceBean查找所有Service，就不用个个写了
        Object serviceInstance = SERVICE_REGISTRY.get(request.getServiceName());
        logger.info(serviceInstance.toString());
        if (serviceInstance == null) {
            response.setStatusCode(400);
            response.setStatusMessage("Service not found");
            protocol.setBody(response);
            ctx.writeAndFlush(protocol);
            return;
        }

        try {
            // 反射调用服务方法
            // todo: 如果找不到方法的话，要返回相应报错
            Method method = serviceInstance.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object result = method.invoke(serviceInstance, request.getParameters());
            response.setResult(result);
            response.setStatusCode(200);
        } catch (Exception e) {
            response.setStatusCode(400);
            response.setError(e.getMessage());
        }

        protocol.setBody(response);
        protocol.getMsgHeader().setEventType(EventType.RESPONSE.getType());
        // 返回结果
        ctx.writeAndFlush(protocol);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            logger.debug("IdleStateEvent triggered, send heartbeat to channel " + ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        cause.printStackTrace();
        ctx.channel().close();
    }
}