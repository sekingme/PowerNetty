 /* power by sekingme */

package org.remoteRpcExample.future;

import org.remoteRpcExample.dto.RpcRequest;
import org.remoteRpcExample.dto.RpcResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * RpcFuture
 */
public class RpcFuture implements Future<Object> {

    private CountDownLatch latch = new CountDownLatch(1);

    private RpcResponse response;

    private RpcRequest request;

    private long requestId;

    public RpcFuture(RpcRequest rpcRequest, long requestId) {
        this.request = rpcRequest;
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public RpcResponse get() throws InterruptedException {
        // the timeout period should be defined by the business party
        boolean success = latch.await(30, TimeUnit.SECONDS);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + this.requestId
                    + ". Request class name: " + this.request.getServiceName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        boolean success = latch.await(timeout, unit);
        if (!success) {
            throw new RuntimeException("Timeout exception. Request id: " + requestId
                    + ". Request class name: " + this.request.getServiceName()
                    + ". Request method: " + this.request.getMethodName());
        }
        return response;
    }

    public void done(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }
}
