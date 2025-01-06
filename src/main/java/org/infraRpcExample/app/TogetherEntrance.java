package org.infraRpcExample.app;

import org.apache.commons.lang3.ClassUtils;
import org.infraRpcExample.config.NettyServerConfig;
import org.infraRpcExample.enums.NodeType;
import org.infraRpcExample.host.Host;
import org.infraRpcExample.protocol.RpcRequest;
import org.infraRpcExample.protocol.RpcResponse;
import org.infraRpcExample.remote.NettyClient;
import org.infraRpcExample.remote.NettyServer;
import org.infraRpcExample.service.ReportService;
import org.infraRpcExample.service.UserDataService;

import java.lang.reflect.Method;

import static org.infraRpcExample.utils.Constants.SUCCESS_CODE;

/**
 * @author: jiangshequan
 * <p>
 * 核心开发人员：远达、亁开、书浩
 * 开发周期：9~10月，9.23前写入抽像类和接口，其它人投入开发，10月底希望上线第一版
 * 支持Netty原因：
 * 1. Kong过于依赖公司的服务，黑箱操作，不利于团队底层技术成长。很多同学逐渐只会写业务代码。
 * 2. 无法控制DP迭代等，比如不能升级JDK21
 * 3. 新DP工程间接口编写都要写两次，冗余代码很多
 * 4. 目前新旧DP两个服务，不方便升级和开发，kong也支持不了两个服务间的调用
 * 5. kong不支持sse请求，要业务方自行选择服务器
 * 支持Netty开发工作内容：
 * 1. 调研测试、架构设计、抽象类和接口的编写 -- sq
 * 2. 实现统一的客户端和服务端，供各微服务初始化；实现连接管理（每个客户端和服务端实现连接池管理，避免频繁创建和销毁连接带来的性能损耗）和心跳检查等; 远程调用方式要适配原有Kong的调用方式以方便平替 -- sq
 * 3. 自定义通信协议并编解码（使用Protostuff，Google提供的高效二进制序列化格式），协议包括header消息头和body消息体
 * 4. 负载均衡（如随机、轮询、一致性哈希等）(加缓存，但升级可能有影响)+服务注册|发现|注销|告警(一期使用ZK，注意服务可能是K8S容器节点，要加上心跳检查和断路器)。这两个一般可以一起做
 * 5. 错误处理，超时与重试策略（预计：远程调用时统一故障转移和实例重试）
 * 6. 测试环境要有相应的实例部署测试（analysis）
 * <p>
 * 实现参考工程：DolphinScheduler(Master&worker&logServer)/Pulsar(客户端&服务器)/Spark(节点间数据传输)
 * <p>
 * 首期应用试点： 1.WEB服务和Analysis服务之间部分请求通信支持试验。
 * 2.新旧DP间RPC通信支持: 应用到事件分析的AI功能等，只需把controller改成从新DP进去
 */
public class TogetherEntrance {
    // 通过启动多线程的方式模拟多进程间通信
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            try {
                NettyServer nettyServer = new NettyServer(NettyServerConfig.builder().build());
                nettyServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Host host = new Host("localhost", 8085);

        // 反射主要为了让项目识别类被应用，避免依赖类被删除。如果没有依赖关系可以跳过反射而直接硬编码RpcRequest
        Method method = ClassUtils.getPublicMethod(UserDataService.class, "getUserData", String.class);
        RpcRequest request1 = RpcRequest.builder()
                .nodeType(NodeType.NETTY_TEST)
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(new Object[]{"Say I love you JSQ!"})
                .build();

        Method method2 = ClassUtils.getPublicMethod(ReportService.class, "getReport", String.class, int.class);
        RpcRequest request2 = RpcRequest.builder()
                .nodeType(NodeType.NETTY_TEST)
                .serviceName(method2.getDeclaringClass().getName())
                .methodName(method2.getName())
                .parameterTypes(method2.getParameterTypes())
                .parameters(new Object[]{"Say you love me JSQ!", 123123123})
                .build();

        RpcResponse<?> rsp1 = NettyClient.getInstance().sendBatchSync(request1, host, 1000L);
        if (null != rsp1 && rsp1.getStatusCode() == SUCCESS_CODE) {
            System.out.println("Result: " + rsp1.getResult());
        }

        RpcResponse<?> rsp2 = NettyClient.getInstance().sendBatchSync(request2, host, 1000L);
        if (null != rsp2 && rsp2.getStatusCode() == SUCCESS_CODE) {
            System.out.println("Result: " + rsp2.getResult());
        }

        NettyClient.getInstance().close();
    }
}