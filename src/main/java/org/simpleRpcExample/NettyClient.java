package org.simpleRpcExample;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClient {
    private final String host;
    private final int port;
    private Channel channel;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
            System.out.println("Client connected to server: " + host + ":" + port);
        } finally {
            // 不关闭group，让客户端保持连接
        }
    }

    public RpcResponse sendRequest(RpcRequest request) throws InterruptedException {
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        return handler.sendRequest(channel, request);
    }
}

// 客户端处理器
class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private RpcResponse response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        this.response = response;
    }

    public RpcResponse sendRequest(Channel channel, RpcRequest request) throws InterruptedException {
        channel.writeAndFlush(request).sync();
        // 等待响应完成
        while (response == null) {
            Thread.sleep(10);
        }
        return response;
    }
}
