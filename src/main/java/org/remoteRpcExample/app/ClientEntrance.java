package org.remoteRpcExample.app;

import org.remoteRpcExample.dto.RpcRequest;
import org.remoteRpcExample.dto.RpcResponse;
import org.remoteRpcExample.remote.NettyClient;
import org.remoteRpcExample.utils.Host;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sekingme
 */
public class ClientEntrance {
    // 多进程通信真实客户端
    public static void main(String[] args) {
        try {
            Host host = new Host("172.19.88.131", 8085);

            ExecutorService executorService = Executors.newFixedThreadPool(5);
            executorService.submit(() -> {
                try {
                    RpcRequest request = new RpcRequest();
                    request.setServiceName("UserDataService");
                    request.setMethodName("getUserData");
                    request.setParameterTypes(new Class[]{String.class});
                    request.setParameters(new Object[]{"Say I love you JSQ!"});
                    // 模拟任务执行
                    runTask(request, host);
//                    Thread.sleep(2000);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task completed.");
            });

            executorService.submit(() -> {
                try {
                    RpcRequest request = new RpcRequest();
                    request.setServiceName("ReportService");
                    request.setMethodName("getReport");
                    request.setParameterTypes(new Class[]{String.class, int.class});
                    request.setParameters(new Object[]{"Say you love me JSQ!", 123123123});
                    // 模拟任务执行
                    runTask(request, host);
//                    Thread.sleep(2000);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task completed.");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTask(RpcRequest request, Host host) throws Exception {
        // 发送请求并接收响应
        RpcResponse<?> response = null;
        try {
            response = NettyClient.getInstance().sendNettyRequest(request, host);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response.getError() == null) {
            System.out.println("Result: " + response.getResult());
        } else {
            System.out.println(response.getError());
        }
    }
}
