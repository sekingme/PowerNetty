package org.remoteRpcExample.remote;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.remoteRpcExample.codec.NettyDecoder;
import org.remoteRpcExample.codec.NettyEncoder;
import org.remoteRpcExample.config.NettyServerConfig;
import org.remoteRpcExample.dto.RpcRequest;
import org.remoteRpcExample.utils.Constants;
import org.remoteRpcExample.utils.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sekingme
 */
public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * boss group
     */
    private final EventLoopGroup bossGroup;

    /**
     * worker group
     */
    private final EventLoopGroup workGroup;

    /**
     * server config
     */
    private final NettyServerConfig serverConfig;

    /**
     * server bootstrap
     */
    private final ServerBootstrap serverBootstrap = new ServerBootstrap();

    /**
     * started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * server init
     *
     * @param serverConfig server config
     */
    public NettyServer(final NettyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        if (NettyUtils.useEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.workGroup = new EpollEventLoopGroup(serverConfig.getWorkerThread(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.workGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        this.start();
        System.out.println("Netty server start successfully.");
    }

    public void start() {
        try {
            if (isStarted.compareAndSet(false, true)) {
                this.serverBootstrap.group(this.bossGroup, this.workGroup)
                        .channel(NettyUtils.getServerSocketChannelClass())
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_BACKLOG, serverConfig.getSoBacklog())
                        .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isSoKeepalive())
                        .childOption(ChannelOption.TCP_NODELAY, serverConfig.isTcpNoDelay())
                        .childOption(ChannelOption.SO_SNDBUF, serverConfig.getSendBufferSize())
                        .childOption(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBufferSize())
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new NettyEncoder());
                                pipeline.addLast(new NettyDecoder(RpcRequest.class));
                                pipeline.addLast("server-idle-handle", new IdleStateHandler(0, 0, Constants.NETTY_SERVER_HEART_BEAT_TIME, TimeUnit.MILLISECONDS));
                                pipeline.addLast(new NettyServerHandler());  // 请求处理器
                            }
                        });
                ChannelFuture future;
                try {
                    future = this.serverBootstrap.bind(serverConfig.getListenPort()).sync();
                } catch (Exception e) {
                    logger.error("NettyRemotingServer bind fail {}, exit", e.getMessage(), e);
                    throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()));
                }
                if (future.isSuccess()) {
                    logger.info("NettyRemotingServer bind success at port : {}", serverConfig.getListenPort());
                } else if (future.cause() != null) {
                    throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()), future.cause());
                } else {
                    throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", serverConfig.getListenPort()));
                }
            }
        } finally {
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
        }
    }

    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            try {
                if (bossGroup != null) {
                    this.bossGroup.shutdownGracefully();
                }
                if (workGroup != null) {
                    this.workGroup.shutdownGracefully();
                }

            } catch (Exception ex) {
                logger.error("netty server close exception", ex);
            }
            logger.info("netty server closed");
        }
    }
}