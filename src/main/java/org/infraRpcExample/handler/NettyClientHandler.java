 /* power by sekingme */

package org.infraRpcExample.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.infraRpcExample.future.BatchResponseFuture;
import org.infraRpcExample.future.StreamResponseFuture;
import org.infraRpcExample.protocol.*;
import org.infraRpcExample.remote.NettyClient;
import org.infraRpcExample.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * netty client request handler
 * 共享handler，必须保证处理器是线程安全且无状态的，可以在多个 ChannelPipeline 中共享
 *
 * @author sekingme
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * netty client
     */
    private final NettyClient nettyClient;

    /**
     * callback thread executor
     */
    private final ExecutorService callbackExecutor;

    public NettyClientHandler(NettyClient nettyClient, ExecutorService callbackExecutor) {
        this.nettyClient = nettyClient;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * When the current channel is not active,
     * the current channel has reached the end of its life cycle
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.warn("Netty client closed channel: " + ChannelUtils.toAddress(ctx.channel()));
        nettyClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.debug("Netty client channelActive: " + ChannelUtils.toAddress(ctx.channel()));
    }

    /**
     * The current channel reads data from the remote
     *
     * @param ctx channel handler context
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol<?> rpcProtocol = (RpcProtocol<?>) msg;
        if (rpcProtocol.getMsgHeader().getEventType() == EventType.STREAM_REQUEST.getType()) {
            processStream((RpcProtocol<RpcResponse<String>>) msg);
        } else {
            processReceived(ctx.channel(), rpcProtocol);
        }
    }

    /**
     * process received logic
     *
     * @param rpcProtocol rpcProtocol
     */
    private void processReceived(final Channel channel, final RpcProtocol<?> rpcProtocol) {
        BatchResponseFuture future = BatchResponseFuture.getFuture(rpcProtocol.getMsgHeader().getOpaque());
        if (future != null) {
            future.setResponse((RpcResponse<?>) rpcProtocol.getBody());
            future.release();
            if (future.getInvokeCallback() != null) {
                // 如果要回调，则回调处理。否则即等待数据完成接收即可
                this.callbackExecutor.submit(future::executeInvokeCallback);
            } else {
                future.putResponse((RpcResponse<?>) rpcProtocol.getBody());
            }
        } else {
            logger.error("Not an excepted netty situation. Please check ResponseFuture.");
        }
    }

    /**
     * process stream data
     *
     * @param rpcProtocol
     */
    private void processStream(final RpcProtocol<RpcResponse<String>> rpcProtocol) {
        String result = rpcProtocol.getBody().getResult();
        long   opaque = rpcProtocol.getMsgHeader().getOpaque();
        StreamResponseFuture.accept(opaque, result);
    }

    /**
     * caught exception
     *
     * @param ctx   channel handler context
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Netty client exceptionCaught", cause);
        nettyClient.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            logger.debug("Netty client userEventTriggered");
            RpcProtocol<RpcRequest> rpcProtocol   = new RpcProtocol<>();
            MessageHeader           messageHeader = new MessageHeader();
            messageHeader.setEventType(EventType.HEARTBEAT.getType());
            rpcProtocol.setMsgHeader(messageHeader);
            rpcProtocol.setBody(RpcRequest.builder().build());
            ctx.channel().writeAndFlush(rpcProtocol).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            if (logger.isDebugEnabled()) {
                logger.debug("Netty client send heart beat to: {}", ChannelUtils.getRemoteAddress(ctx.channel()));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
