package org.infraRpcExample.protocol;

import lombok.Builder;
import lombok.Data;
import org.infraRpcExample.enums.NodeType;

import java.io.Serializable;

/**
 * @author sekingme
 */
@Data
@Builder
public class RpcRequest implements Serializable {
    private NodeType   nodeType;
    private String     serviceName;
    private String     methodName;
    private Class<?>[] parameterTypes;
    private Object[]   parameters;

    // Getters and setters
}