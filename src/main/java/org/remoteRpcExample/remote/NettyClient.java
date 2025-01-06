package org.remoteRpcExample.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.remoteRpcExample.RpcProtocol.EventType;
import org.remoteRpcExample.RpcProtocol.MessageHeader;
import org.remoteRpcExample.RpcProtocol.RpcProtocol;
import org.remoteRpcExample.codec.NettyDecoder;
import org.remoteRpcExample.codec.NettyEncoder;
import org.remoteRpcExample.codec.RpcSerializer;
import org.remoteRpcExample.config.NettyClientConfig;
import org.remoteRpcExample.config.RpcRequestCache;
import org.remoteRpcExample.config.RpcRequestTable;
import org.remoteRpcExample.dto.RpcRequest;
import org.remoteRpcExample.dto.RpcResponse;
import org.remoteRpcExample.future.RpcFuture;
import org.remoteRpcExample.utils.Constants;
import org.remoteRpcExample.utils.Host;
import org.remoteRpcExample.utils.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Sekingme
 */
public class NettyClient {
    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public static NettyClient getInstance() {
        return NettyClient.NettyClientInner.INSTANCE;
    }

    private static class NettyClientInner {
        private static final NettyClient INSTANCE = new NettyClient(new NettyClientConfig());
    }

    /**
     * client config
     */
    private final NettyClientConfig clientConfig;

    /**
     * worker group
     */
    private final EventLoopGroup workerGroup;

    /**
     * client bootstrap
     */
    private final Bootstrap bootstrap = new Bootstrap();

    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap(4);

    /**
     * started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * get channel
     */
    private Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if (channel != null && channel.isActive()) {
            return channel;
        }
        return createChannel(host, true);
    }

    /**
     * create channel
     *
     * @param host   host
     * @param isSync sync flag
     * @return channel
     */
    public Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            synchronized (this.bootstrap) {
                future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            }
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channels.put(host, channel);
                return channel;
            }
        } catch (Exception ex) {
            System.out.println(String.format("connect to %s error", host));
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * client init
     *
     * @param clientConfig client config
     */
    private NettyClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (NettyUtils.useEpoll()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Netty client start successfully.");
    }

    public void start() throws Exception{
        try {
            this.bootstrap.group(this.workerGroup)
                    .channel(NettyUtils.getSocketChannelClass())
                    .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                    .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                    .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                    .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyEncoder());
                            pipeline.addLast(new NettyDecoder(RpcResponse.class));
                            pipeline.addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS));
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            isStarted.compareAndSet(false, true);
        } finally {
            // 不关闭group，让客户端保持连接
        }
    }

    public RpcResponse<?> sendNettyRequest(RpcRequest request, Host host) throws Exception {
        Channel channel = getChannel(host);
        assert channel != null;

        RpcProtocol<RpcRequest> protocol = buildProtocol(request);

        RpcRequestCache rpcRequestCache = new RpcRequestCache();
        long reqId = protocol.getMsgHeader().getRequestId();
        RpcFuture future = new RpcFuture(request, reqId);
        rpcRequestCache.setRpcFuture(future);

        RpcRequestTable.put(protocol.getMsgHeader().getRequestId(), rpcRequestCache);
        channel.writeAndFlush(protocol).sync();

        // todo: 看需求情况后续再加入异步处理逻辑
        RpcResponse<?> result = null;
        try {
            // 同步则获取结果
            result = future.get();
        } catch (InterruptedException e) {
            logger.error("send msg error，service name is {}", request.getServiceName(), e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private RpcProtocol<RpcRequest> buildProtocol(RpcRequest req) {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        MessageHeader header = new MessageHeader();
        header.setRequestId(RpcRequestTable.getRequestId());
        header.setEventType(EventType.REQUEST.getType());
        header.setSerialization(RpcSerializer.PROTOSTUFF.getType());
        protocol.setMsgHeader(header);
        protocol.setBody(req);
        return protocol;
    }

    /**
     * close
     */
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                closeChannels();
                if (workerGroup != null) {
                    this.workerGroup.shutdownGracefully();
                }
            } catch (Exception ex) {
                logger.error("netty client close exception", ex);
            }
            logger.info("netty client closed");
        }
    }

    /**
     * close channels
     */
    private void closeChannels() {
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }
}
