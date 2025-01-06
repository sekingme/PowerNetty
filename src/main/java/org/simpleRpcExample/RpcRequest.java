package org.simpleRpcExample;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcRequest implements Serializable {
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    // Getters and setters
}