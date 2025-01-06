 /* power by sekingme */

package org.remoteRpcExample.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RpcRequestTable
 */
public class RpcRequestTable {

    private static AtomicLong requestIdGen = new AtomicLong(0);
    private static ConcurrentHashMap<Long, RpcRequestCache> requestMap = new ConcurrentHashMap<>();

    private RpcRequestTable() {
        throw new IllegalStateException("Utility class");
    }

    public static void put(long requestId, RpcRequestCache rpcRequestCache) {
        requestMap.put(requestId, rpcRequestCache);
    }

    public static RpcRequestCache get(Long requestId) {
        return requestMap.get(requestId);
    }

    public static void remove(Long requestId) {
        requestMap.remove(requestId);
    }

    public static long getRequestId() {
        return requestIdGen.incrementAndGet();
    }

}
