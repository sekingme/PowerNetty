 /* power by sekingme */

package org.infraRpcExample.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.infraRpcExample.handler.scanner.ServiceScanner;
import org.infraRpcExample.protocol.*;
import org.infraRpcExample.remote.NettyServer;
import org.infraRpcExample.thread.ThreadPoolManager;
import org.infraRpcExample.utils.ChannelUtils;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

import static org.infraRpcExample.utils.Constants.*;


/**
 * netty server request handler
 *
 * @author sekingme
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final ThreadPoolManager SERVER_HANDLER_POOL = ThreadPoolManager.INSTANCE;

    /**
     * netty remote server
     */
    private final NettyServer nettyServer;

    public NettyServerHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    /**
     * When the current channel is not active,
     * the current channel has reached the end of its life cycle
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.warn("Netty server close channel: " + ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Netty server channel active: " + ChannelUtils.toAddress(ctx.channel()));
    }

    /**
     * The current channel reads data from the remote end
     *
     * @param ctx channel handler context
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol<RpcRequest> rpcProtocol = (RpcProtocol<RpcRequest>) msg;
        if (EventType.HEARTBEAT.getType() == rpcProtocol.getMsgHeader().getEventType()) {
            logger.info("Netty server receive heart beat from: host: {}", ChannelUtils.getRemoteAddress(ctx.channel()));
            return;
        }
        SERVER_HANDLER_POOL.addExecuteTask(() -> processReceived(ctx, rpcProtocol));
    }

    /**
     * process received logic
     */
    private void processReceived(ChannelHandlerContext ctx, RpcProtocol protocol) {
        RpcResponse<Object> response = new RpcResponse<>();
        RpcRequest          request  = (RpcRequest) protocol.getBody();

        String protocolStr = null;
        try {
            protocolStr = ObjectMapperConfig.getCommonObjectMapper().writeValueAsString(protocol);
            Object serviceInstance = ServiceScanner.getServiceMap().get(request.getServiceName());
            if (serviceInstance == null) {
                logger.error("service {} can not be found.", request.getServiceName());
                protocol.getMsgHeader().setEventType(EventType.RESPONSE.getType());
                response.setStatusCode(NOT_FOUND_CODE);
                response.setStatusMessage("Service not found");
                protocol.setBody(response);
                ctx.writeAndFlush(protocol);
                return;
            }

            Method method = serviceInstance.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            if (protocol.getMsgHeader().getEventType() == org.infraRpcExample.protocol.EventType.STREAM_REQUEST.getType()) {
                Object result = method.invoke(serviceInstance, request.getParameters());
                if (result instanceof SseEmitter) {
                    SeeEmitterDealer seeEmitterHandler = new SeeEmitterDealer(protocol.getMsgHeader().getOpaque());
                    seeEmitterHandler.deal((SseEmitter) result, ctx);
                    return;
                }
            } else {
                Object result = method.invoke(serviceInstance, request.getParameters());
                response.setResult(result);
                response.setStatusCode(SUCCESS_CODE);
            }
        } catch (Exception e) {
            response.setStatusCode(INTERNAL_SERVER_ERROR_CODE);
            response.setError(e.getMessage());
            logger.error("Failed to processReceived data from netty client. RpcProtocol:{}", protocolStr, e);
        }

        protocol.setBody(response);
        protocol.getMsgHeader().setEventType(EventType.RESPONSE.getType());
        ctx.writeAndFlush(protocol);
    }

    /**
     * caught exception
     *
     * @param ctx   channel handler context
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Netty server exceptionCaught." + cause.getMessage());
        ctx.channel().close();
    }

    /**
     * channel write changed
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Netty server channelWritabilityChanged");
        Channel       ch     = ctx.channel();
        ChannelConfig config = ch.config();

        if (!ch.isWritable()) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is not writable, over high water level : {}",
                        ch, config.getWriteBufferHighWaterMark());
            }

            config.setAutoRead(false);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is writable, to low water : {}",
                        ch, config.getWriteBufferLowWaterMark());
            }
            config.setAutoRead(true);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        logger.debug("Netty server userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * @author zhushuhao
     * @description: SeeEmitter 反射方式适配其数据发送方式为netty
     * @date 2024/10-14 15:35
     */
    static class SeeEmitterDealer {

        private final long opaque;

        public SeeEmitterDealer(long opaque) {
            this.opaque = opaque;
        }

        public SseEmitter deal(SseEmitter sseEmitter, ChannelHandlerContext ctx) throws Exception {
            // 获取父类
            Class<?> superclass = SseEmitter.class.getSuperclass();

            // 获取私有变量handler
            Field handlerField = superclass.getDeclaredField("handler");
            // 设置私有变量可访问
            handlerField.setAccessible(true);
            // 修改私有变量的值
            Object newHandlerValue = createHandlerProxy(handlerField.getType(), ctx);
            handlerField.set(sseEmitter, newHandlerValue);
            return sseEmitter;
        }

        private Object createHandlerProxy(Class<?> handlerInterface, ChannelHandlerContext ctx) {
            // 创建动态代理实例
            return Proxy.newProxyInstance(
                    handlerInterface.getClassLoader(),
                    new Class<?>[]{handlerInterface},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "send":
                                    handleSend(args, ctx);
                                    break;
                                case "complete":
                                    handleComplete(ctx);
                                    break;
                                case "completeWithError":
                                    handleCompleteWithError(args, ctx);
                                    break;
                                case "onTimeout":
                                    handleOnTimeout(args, ctx);
                                    break;
                                case "onError":
                                    handleOnError(args, ctx);
                                    break;
                                case "onCompletion":
                                    handleOnCompletion(args, ctx);
                                    break;
                                default:
                                    throw new UnsupportedOperationException("未实现的方法: " + method.getName());
                            }
                            return null; // 根据需要返回合适的值
                        }
                    }
            );
        }

        private void handleSend(Object[] args, ChannelHandlerContext ctx) {
            Object    data      = args[0];
            MediaType mediaType = (MediaType) args[1];
            if (data instanceof String) {
                String result = (String) data;
                if (DATA_BEGIN.equalsIgnoreCase(result) || DOUBLE_NEW_LINE.equals(result)) {
                    return;
                }
                RpcResponse<String> response = new RpcResponse<>();
                response.setStatusCode(SUCCESS_CODE);
                RpcProtocol<RpcResponse<String>> protocol      = new RpcProtocol<>();
                MessageHeader                    messageHeader = new MessageHeader();
                messageHeader.setEventType(EventType.STREAM_REQUEST.getType());
                messageHeader.setOpaque(opaque);
                protocol.setMsgHeader(messageHeader);
                response.setResult(result);
                protocol.setBody(response);
                ctx.writeAndFlush(protocol);
            }
        }

        private void handleComplete(ChannelHandlerContext ctx) {
            // 实现 complete 方法的逻辑
            ctx.flush();
        }

        private void handleCompleteWithError(Object[] args, ChannelHandlerContext ctx) {
            Throwable failure = (Throwable) args[0];
            // 实现 completeWithError 方法的逻辑
            ctx.writeAndFlush(failure);
            ctx.close();
        }

        private void handleOnTimeout(Object[] args, ChannelHandlerContext ctx) {
            Runnable callback = (Runnable) args[0];
            // 实现 onTimeout 方法的逻辑
            // 调用回调
            if (callback != null) {
                callback.run();
            }
            ctx.close();
        }

        private void handleOnError(Object[] args, ChannelHandlerContext ctx) {
            Consumer<Throwable> callback = (Consumer<Throwable>) args[0];
            // 实现 onError 方法的逻辑
            // 调用回调
            if (callback != null) {
                callback.accept(new Throwable("模拟错误"));
            }
            ctx.close();
        }

        private void handleOnCompletion(Object[] args, ChannelHandlerContext ctx) {
            Runnable callback = (Runnable) args[0];
            // 实现 onCompletion 方法的逻辑
            // 调用回调
            if (callback != null) {
                callback.run();
            }
        }

    }
}
