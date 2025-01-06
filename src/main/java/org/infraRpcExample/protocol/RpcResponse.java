package org.infraRpcExample.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sekingme
 */
@Data
public class RpcResponse<T> implements Serializable {
    // 状态码（如200表示成功，500表示服务器错误等）
    private int statusCode;

    // 状态消息或描述（如 "Success", "Internal Server Error"）
    private String statusMessage;

    // 请求处理的结果数据
    private T result;

    // 详细的错误信息
    private String error;

    // Getters and setters
}
