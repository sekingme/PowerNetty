 /* power by sekingme */

package org.infraRpcExample.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.infraRpcExample.codec.NettyDecoder;
import org.infraRpcExample.codec.NettyEncoder;
import org.infraRpcExample.config.NettyClientConfig;
import org.infraRpcExample.exceptions.RemotingException;
import org.infraRpcExample.exceptions.RemotingTimeoutException;
import org.infraRpcExample.future.BatchResponseFuture;
import org.infraRpcExample.future.StreamResponseFuture;
import org.infraRpcExample.handler.NettyClientHandler;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.host.hostmanager.interfaces.HostManager;
import org.infraRpcExample.protocol.*;
import org.infraRpcExample.thread.CallerThreadExecutePolicy;
import org.infraRpcExample.thread.NamedThreadFactory;
import org.infraRpcExample.utils.Constants;
import org.infraRpcExample.utils.NettyUtils;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.infraRpcExample.utils.Constants.HUNDRED_TIME_MILLIS;

/**
 * remoting netty client
 *
 * @author sekingme
 */
public class NettyClient {

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    /**
     * client bootstrap
     */
    private final Bootstrap bootstrap = new Bootstrap();
    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap<>(128);
    /**
     * started flag
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    /**
     * worker group
     */
    private final EventLoopGroup workerGroup;
    /**
     * client config
     */
    private final NettyClientConfig clientConfig;
    /**
     * callback thread executor
     */
    private final ExecutorService callbackExecutor;
    /**
     * client handler
     */
    private final NettyClientHandler clientHandler;
    /**
     * response future executor
     */
    private final ScheduledExecutorService responseFutureExecutor;
    /**
     * host manager from HostManagerConfig
     */
    @Autowired
    private HostManager hostManager;

    /**
     * client init
     *
     * @param clientConfig client config
     */
    public NettyClient(final NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        if (NettyUtils.useEpoll()) {
            this.workerGroup = new EpollEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.workerGroup = new NioEventLoopGroup(clientConfig.getWorkerThreads(), new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
                }
            });
        }
        // 处理回调逻辑，如无相关逻辑可以不处理
        this.callbackExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(1000), new NamedThreadFactory("CallbackExecutor", 10),
                new CallerThreadExecutePolicy());
        this.clientHandler = new NettyClientHandler(this, callbackExecutor);
        // 定期清理失效的请求
        this.responseFutureExecutor =  Executors.newScheduledThreadPool(2, new NamedThreadFactory("ResponseFutureExecutor"));

        this.start();
    }

    public static NettyClient getInstance() {
        return NettyClient.NettyClientInner.INSTANCE;
    }

    @Deprecated
    public ConcurrentHashMap<Host, Channel> getMap() {
        return channels;
    }

    /**
     * start
     */
    private void start() {
        this.bootstrap
                .group(this.workerGroup)
                .channel(NettyUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("client-idle-handler", new IdleStateHandler(Constants.NETTY_CLIENT_HEART_BEAT_TIME, 0, 0, TimeUnit.MILLISECONDS))
                                .addLast(new NettyDecoder(RpcResponse.class), clientHandler, new NettyEncoder());
                    }
                });
        this.responseFutureExecutor.scheduleAtFixedRate(BatchResponseFuture::scanFutureTable, 30, 30, TimeUnit.SECONDS);
        this.responseFutureExecutor.scheduleAtFixedRate(StreamResponseFuture::scanFutureConsumer, 120, 30, TimeUnit.SECONDS);

        isStarted.compareAndSet(false, true);
    }

    /**
     * sync send task for batch request (no return)
     *
     * @param host    host
     * @param request request
     */
    public void sendOnlySync(RpcRequest request, Host host) throws Exception {
        int retries = 3;
        while (retries-- > 0) {
            if (null == host) {
                host = hostManager.select(request);
            }
            // todo: 只发送，不返回
        }
    }

    /**
     * sync send for batch request
     *
     * @param host          host  todo:  生产环境中可以删除host参数
     * @param request       request
     * @param timeoutMillis timeoutMillis
     * @return command
     */
    public RpcResponse<?> sendBatchSync(RpcRequest request, Host host, final long timeoutMillis) throws Exception {
        RpcResponse<?> rsp     = null;
        int            retries = 3;
        while (retries-- > 0) {
            if (null == host) {
                host = hostManager.select(request);
            }
            rsp = send(request, host, timeoutMillis);
            //success
            if (null != rsp && rsp.getStatusCode() == Constants.SUCCESS_CODE) {
                break;
            } else {
                logger.warn("netty request failed, retry: {}", retries);
                Thread.sleep(HUNDRED_TIME_MILLIS);
            }
        }
        if (retries <= 0) {
            String errorMsg = rsp == null ? "Failed to send or response for netty request" : rsp.getError();
            logger.error(errorMsg + " request: {}, host: {}",
                    ObjectMapperConfig.getCommonObjectMapper().writeValueAsString(request),
                    host.getAddress());
            throw new Exception(errorMsg);
        }
        return rsp;
    }

    public RpcResponse<?> send(RpcRequest request, Host host, long timeoutMillis) throws Exception {
        Channel channel = getChannel(host);
//        channels.forEach((key, value) -> System.out.println(key + "::" + value));
        if (channel == null) {
            logger.error("Netty channel is null for host: {}.", host.getAddress());
            throw new RemotingException(String.format("get channel with host: %s fail", host));
        }

        RpcProtocol<RpcRequest> protocol = buildProtocol(request, EventType.BATCH_REQUEST.getType());
//        System.out.println(ObjectMapperConfig.getCommonObjectMapper().writeValueAsString(protocol));

        final long                opaque              = protocol.getMsgHeader().getOpaque();
        final BatchResponseFuture batchResponseFuture = new BatchResponseFuture(opaque, timeoutMillis, null, null);
        channel.writeAndFlush(protocol).addListener(future -> {
            if (future.isSuccess()) {
                batchResponseFuture.setSendOk(true);
                return;
            } else {
                batchResponseFuture.setSendOk(false);
            }
            batchResponseFuture.setCause(future.cause());
            batchResponseFuture.putResponse(null);
            logger.error("send RpcProtocol {} to host {} failed.", protocol, host);
        });
        /*
         * sync wait for result
         */
        RpcResponse<?> result = batchResponseFuture.waitResponse();
        if (result == null) {
            if (batchResponseFuture.isSendOK()) {
                throw new RemotingTimeoutException(host.toString(), timeoutMillis, batchResponseFuture.getCause());
            } else {
                throw new RemotingException(host.toString(), batchResponseFuture.getCause());
            }
        }
        return result;
    }

    /**
     * sync send for stream request
     *
     * @param host          host
     * @param request       request
     * @param timeoutMillis timeoutMillis
     * @return command
     */
    public CompletableFuture<Void> sendSseSync(RpcRequest request, Host host, long timeoutMillis, Consumer<String> dataConsumer) throws Exception {
        Channel channel = getChannel(host);
        if (channel == null) {
            logger.error("Netty channel is null for host: {}.", host.getAddress());
            throw new RemotingException(String.format("Netty get channel with host: %s fail", host));
        }

        RpcProtocol<RpcRequest> protocol = buildProtocol(request, EventType.STREAM_REQUEST.getType());

        final long opaque = protocol.getMsgHeader().getOpaque();

        StreamResponseFuture.putConsumer(opaque, timeoutMillis, dataConsumer);

        channel.writeAndFlush(protocol);

        return receiveDataAsync(channel);
    }

    public CompletableFuture<Void> receiveDataAsync(Channel channel) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        channel.closeFuture().addListener(f -> {
            if (f.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(f.cause());
            }
        });

        return future;
    }

    private RpcProtocol<RpcRequest> buildProtocol(RpcRequest req, byte eventType) {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        MessageHeader           header   = new MessageHeader();
        header.setEventType(eventType);
        // todo: 通过UserUtil.getUsername() 和UserUtil.getUserWorkId()获取可能的用户名和工号，如果获取不到则为空即可
        protocol.setMsgHeader(header);
        protocol.setBody(req);
        return protocol;
    }

    /**
     * get channel
     */
    private synchronized Channel getChannel(Host host) {
        try {
            Channel channel = channels.get(host);
            if (channel != null && channel.isActive()) {
                logger.debug("Netty return existed channel for host: {}", host.getAddress());
                return channel;
            }
        } catch (Exception e) {
            logger.error("Failed to getChannel.", e);
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
    private Channel createChannel(Host host, boolean isSync) {
        ChannelFuture future;
        try {
            future = bootstrap.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            if (isSync) {
                future.sync();
            }
            if (future.isSuccess()) {
                logger.info("Succeed to connect to host " + host);
                Channel channel = future.channel();
                if (channels.containsKey(host)) {
                    logger.warn("[warn] repeat write channel to ConcurrentHashMap.");
                }
                channels.put(host, channel);
                return channel;
            }
        } catch (Exception ex) {
            logger.error("Failed to create channel and connect to {}. ", host, ex);
        }
        return null;
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
                if (callbackExecutor != null) {
                    this.callbackExecutor.shutdownNow();
                }
                if (this.responseFutureExecutor != null) {
                    this.responseFutureExecutor.shutdownNow();
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

    /**
     * close channel
     *
     * @param host host
     */
    public void closeChannel(Host host) {
        Channel channel = this.channels.remove(host);
        if (channel != null) {
            channel.close();
        }
    }

    private static class NettyClientInner {
        private static final NettyClient INSTANCE = new NettyClient(NettyClientConfig.builder().build());
    }
}
