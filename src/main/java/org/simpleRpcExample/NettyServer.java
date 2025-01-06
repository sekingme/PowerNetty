package org.simpleRpcExample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.Method;

/**
 * @author sekingme
 */
public class NettyServer {
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ServerHandler());  // 请求处理器
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Server started on port: " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class ServerHandler extends ChannelInboundHandlerAdapter {
    // 假设目标服务类的实例
    private final ServiceClass service = new ServiceClass();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("--------------");
        RpcResponse response = new RpcResponse();
        try {
            if (msg instanceof RpcRequest) {  // 判断消息类型
                RpcRequest request = (RpcRequest) msg;  // 手动进行类型转换
                Object result = invokeMethod(request);  // 调用方法
                response.setResult(result);
            } else {
                response.setError(new IllegalArgumentException("Unsupported message type"));
            }
        } catch (Throwable t) {
            response.setError(t);
        }
        ctx.writeAndFlush(response);  // 发送响应
    }

    // 调用外部类的方法
    private Object invokeMethod(RpcRequest request) throws Exception {
        // 获取服务类的 Class 对象
        Class<?> serviceClass = service.getClass();
        // 通过方法名和参数类型查找具体的方法
        Method method = serviceClass.getMethod(request.getMethodName(), request.getParameterTypes());
        // 使用服务类的实例调用该方法，并传入参数
        return method.invoke(service, request.getParameters());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

//// 服务端处理器，简单处理器
class SimpleServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ServiceClass service = new ServiceClass();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse response = new RpcResponse();
        try {
            // 通过反射调用方法
            Object result = invokeMethod(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t);
        }
        ctx.writeAndFlush(response);
    }

    // 调用外部类的方法
    private Object invokeMethod(RpcRequest request) throws Exception {
        // 获取服务类的 Class 对象
        Class<?> serviceClass = service.getClass();
        // 通过方法名和参数类型查找具体的方法
        Method method = serviceClass.getMethod(request.getMethodName(), request.getParameterTypes());
        // 使用服务类的实例调用该方法，并传入参数
        return method.invoke(service, request.getParameters());
    }
}