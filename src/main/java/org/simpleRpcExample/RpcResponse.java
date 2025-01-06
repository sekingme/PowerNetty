package org.simpleRpcExample;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse implements Serializable {
    private Object result;
    private Throwable error;

    // Getters and setters
}
