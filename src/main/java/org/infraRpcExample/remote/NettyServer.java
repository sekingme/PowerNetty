 /* power by sekingme */

package org.infraRpcExample.remote;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.infraRpcExample.codec.NettyDecoder;
import org.infraRpcExample.codec.NettyEncoder;
import org.infraRpcExample.config.NettyServerConfig;
import org.infraRpcExample.exceptions.RemoteException;
import org.infraRpcExample.handler.NettyServerHandler;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.utils.Constants;
import org.infraRpcExample.utils.NettyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * remoting netty server
 *
 * @author sekingme
 */
public class NettyServer {

    /**
     * Netty server bind fail message
     */
    private static final String NETTY_BIND_FAILURE_MSG = "NettyRemotingServer bind %s fail";
    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    /**
     * server bootstrap
     */
    private final ServerBootstrap serverBootstrap = new ServerBootstrap();
    /**
     * default executor
     */
    private final ExecutorService defaultExecutor = Executors.newFixedThreadPool(Constants.CPUS);
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
     * server handler
     */
    private final NettyServerHandler serverHandler = new NettyServerHandler(this);
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
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.workGroup = new EpollEventLoopGroup(serverConfig.getWorkerThread(), new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.workGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerThread_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
    }

    /**
     * server start
     */
    public void start() {
        if (isStarted.compareAndSet(false, true)) {
            this.serverBootstrap
                    .group(this.bossGroup, this.workGroup)
                    .channel(NettyUtils.getServerSocketChannelClass())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, serverConfig.getSoBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isSoKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, serverConfig.isTcpNoDelay())
                    .childOption(ChannelOption.SO_SNDBUF, serverConfig.getSendBufferSize())
                    .childOption(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBufferSize())
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            initNettyChannel(ch);
                        }
                    });

            ChannelFuture future;
            try {
                future = serverBootstrap.bind(serverConfig.getListenPort()).sync();
            } catch (Exception e) {
                logger.error("NettyRemotingServer bind fail {}, exit", e.getMessage(), e);
                throw new RemoteException(String.format(NETTY_BIND_FAILURE_MSG, serverConfig.getListenPort()));
            }
            if (future.isSuccess()) {
                logger.info("NettyRemotingServer bind success at port : " + serverConfig.getListenPort());
            } else if (future.cause() != null) {
                throw new RemoteException(String.format(NETTY_BIND_FAILURE_MSG, serverConfig.getListenPort()), future.cause());
            } else {
                throw new RemoteException(String.format(NETTY_BIND_FAILURE_MSG, serverConfig.getListenPort()));
            }
        }
    }

    /**
     * init netty channel
     *
     * @param ch socket channel
     */
    private void initNettyChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast("encoder", new NettyEncoder())
                .addLast("decoder", new NettyDecoder(RpcRequest.class))
                .addLast("server-idle-handle", new IdleStateHandler(0, 0, Constants.NETTY_SERVER_HEART_BEAT_TIME, TimeUnit.MILLISECONDS))
                .addLast("handler", serverHandler);
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
                defaultExecutor.shutdown();
            } catch (Exception ex) {
                logger.error("netty server close exception", ex);
            }
            logger.info("netty server closed");
        }
    }
}
