package org.simpleRpcExample;


public class SimpleMain {
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                new NettyServer(8080).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 启动客户端
        NettyClient client = new NettyClient("localhost", 8080);
        client.start();

        // 构建请求
        RpcRequest request = new RpcRequest();
        request.setMethodName("exampleMethod");  // 方法名
        request.setParameterTypes(new Class[]{String.class, int.class});  // 参数类型数组
        request.setParameters(new Object[]{"Test", 123});  // 参数值数组

        // 发送请求并接收响应
        RpcResponse response = client.sendRequest(request);
        if (response.getError() == null) {
            System.out.println("Result: " + response.getResult());
        } else {
            response.getError().printStackTrace();
        }
    }
}