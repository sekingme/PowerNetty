 /* power by sekingme */
package org.infraRpcExample.future;

/**
 * invoke callback
 */
public interface InvokeCallback {

    /**
     * operation
     *
     * @param batchResponseFuture responseFuture
     */
    void operationComplete(final BatchResponseFuture batchResponseFuture);

}
