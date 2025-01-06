package org.remoteRpcExample.service.impl;

public class ServiceClass {
    // 在外部类中定义的 exampleMethod，支持多个不同类型的参数
    public String exampleMethod(String input, int number) {
        return "Hello, " + input + "! You provided number: " + number;
    }

    // 可以在此类中定义其他服务方法
    public int add(int a, int b) {
        return a + b;
    }
}
