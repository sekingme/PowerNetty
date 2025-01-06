 /* power by sekingme */

package org.infraRpcExample.future;

import org.infraRpcExample.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * response future
 *
 * @author sekingme
 */
public class BatchResponseFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResponseFuture.class);

    private static final ConcurrentHashMap<Long, BatchResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    /**
     * request unique identification
     */
    private final long opaque;

    /**
     * timeout
     */
    private final long timeoutMillis;

    /**
     * invokeCallback function
     */
    private final InvokeCallback invokeCallback;

    /**
     * releaseSemaphore
     */
    private final ReleaseSemaphore releaseSemaphore;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    /**
     * response command
     */
    private RpcResponse<?> response;

    private volatile boolean sendOk = true;

    private Throwable cause;

    public BatchResponseFuture(long opaque, long timeoutMillis, InvokeCallback invokeCallback, ReleaseSemaphore releaseSemaphore) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.releaseSemaphore = releaseSemaphore;
        FUTURE_TABLE.put(opaque, this);
    }

    public static BatchResponseFuture getFuture(long opaque) {
        return FUTURE_TABLE.get(opaque);
    }

    /**
     * scan future table
     */
    public static void scanFutureTable() {
        final List<BatchResponseFuture>                futureList = new LinkedList<>();
        Iterator<Map.Entry<Long, BatchResponseFuture>> it         = FUTURE_TABLE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, BatchResponseFuture> next   = it.next();
            BatchResponseFuture                  future = next.getValue();
            if ((future.getBeginTimestamp() + future.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                futureList.add(future);
                it.remove();
                LOGGER.warn("remove timeout request : {}", future);
            }
        }
        for (BatchResponseFuture future : futureList) {
            try {
                future.release();
                future.executeInvokeCallback();
            } catch (Exception ex) {
                LOGGER.warn("scanFutureTable, execute callback error", ex);
            }
        }
    }

    /**
     * wait for response
     *
     * @return command
     */
    public RpcResponse<?> waitResponse() throws InterruptedException {
        this.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }

    /**
     * put response
     *
     * @param response
     */
    public void putResponse(final RpcResponse response) {
        this.response = response;
        this.latch.countDown();
        FUTURE_TABLE.remove(opaque);
    }

    /**
     * whether timeout
     *
     * @return timeout
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    /**
     * execute invoke callback
     */
    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public boolean isSendOK() {
        return sendOk;
    }

    public void setSendOk(boolean sendOk) {
        this.sendOk = sendOk;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public RpcResponse<?> getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    /**
     * release
     */
    public void release() {
        if (this.releaseSemaphore != null) {
            this.releaseSemaphore.release();
        }
    }

    @Override
    public String toString() {
        return "ResponseFuture{"
                + "opaque=" + opaque
                + ", timeoutMillis=" + timeoutMillis
                + ", invokeCallback=" + invokeCallback
                + ", releaseSemaphore=" + releaseSemaphore
                + ", latch=" + latch
                + ", beginTimestamp=" + beginTimestamp
                + ", response=" + response
                + ", sendOk=" + sendOk
                + ", cause=" + cause
                + '}';
    }
}
