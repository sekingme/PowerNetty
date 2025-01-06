package org.remoteRpcExample.remote;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.remoteRpcExample.RpcProtocol.EventType;
import org.remoteRpcExample.RpcProtocol.MessageHeader;
import org.remoteRpcExample.RpcProtocol.RpcProtocol;
import org.remoteRpcExample.common.ThreadPoolManager;
import org.remoteRpcExample.config.RpcRequestCache;
import org.remoteRpcExample.config.RpcRequestTable;
import org.remoteRpcExample.dto.RpcResponse;
import org.remoteRpcExample.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Sekingme
 * @description:
 * @create: 2024-09-10 14:51
 **/
// 客户端处理器 共享handler，必须保证处理器是线程安全的，可以在多个 ChannelPipeline 中共享。生产环境作为减少开销的优化手段，正常应该不同的channel和不同的pipeline初始化不同的handler
@ChannelHandler.Sharable
class ClientHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private static final ThreadPoolManager THREAD_POOL_MANAGER = ThreadPoolManager.INSTANCE;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProtocol<?> rpcProtocol = (RpcProtocol<?>) msg;

        RpcResponse<?> rsp = (RpcResponse<?>) rpcProtocol.getBody();
        long reqId = rpcProtocol.getMsgHeader().getRequestId();
        RpcRequestCache rpcRequest = RpcRequestTable.get(reqId);
        THREAD_POOL_MANAGER.addExecuteTask(() -> msgHandler(rsp, rpcRequest, reqId));
    }

    private void msgHandler(RpcResponse<?> rsp, RpcRequestCache rpcRequest, long reqId) {
        RpcFuture future = rpcRequest.getRpcFuture();
        RpcRequestTable.remove(reqId);
        future.done(rsp);

        // todo: 后续可以加callback逻辑
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            RpcProtocol<?> rpcProtocol = new RpcProtocol<>();
            MessageHeader messageHeader = new MessageHeader();
            messageHeader.setEventType(EventType.HEARTBEAT.getType());
            rpcProtocol.setMsgHeader(messageHeader);
            ctx.channel().writeAndFlush(rpcProtocol);
            logger.debug("send heart beat msg...");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught : {}", cause.getMessage(), cause);
        ctx.channel().close();
    }
}
