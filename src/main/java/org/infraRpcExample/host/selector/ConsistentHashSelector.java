package org.infraRpcExample.host.selector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infraRpcExample.host.HostWorker;
import org.infraRpcExample.protocol.RpcRequest;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Yuanda Liao
 * Time:   2024/9/25 20:07
 * <p>
 * 一致性hash选择器
 */
@Slf4j
public class ConsistentHashSelector extends AbstractSelector<HostWorker> {

    private final TreeMap<Long, HostWorker> virtualHostWorkers;

    /**
     * 虚拟节点数
     */
    private final int replicaNumber = 160;

    @Getter
    private final int identityHashCode;

    public ConsistentHashSelector(Collection<HostWorker> source, int identityHashCode) {
        this.virtualHostWorkers = new TreeMap<>();
        this.identityHashCode = identityHashCode;

        for (HostWorker hostWorker : source) {
            // 这里除4主要是为了减少MD5的次数，使得16位的MD5可以的到充分的利用
            for (int i = 0; i < replicaNumber / 4; i++) {
                byte[] digest = getMD5(hostWorker.getAddress() + i);
                for (int h = 0; h < 4; h++) {
                    long m = hash(digest, h);
                    virtualHostWorkers.put(m, hostWorker);
                }
            }
        }
    }

    @Override
    protected HostWorker doSelect(Collection<HostWorker> source, RpcRequest request) {
        // 通过请求的方法名和参数来生成一个唯一的key
        StringBuilder keySb = new StringBuilder();
        keySb.append(request.getServiceName());
        keySb.append(request.getMethodName());
        if (null != request.getParameters()) {
            for (Object arg : request.getParameters()) {
                keySb.append(arg);
            }
        }
        byte[] digest = getMD5(keySb.toString());
        return selectForKey(hash(digest, 0));
    }

    private HostWorker selectForKey(long hash) {
        // 返回 >= 给定键的最小键的关联值
        Map.Entry<Long, HostWorker> entry = virtualHostWorkers.ceilingEntry(hash);
        if (entry == null) {
            entry = virtualHostWorkers.firstEntry();
        }
        return entry.getValue();
    }

    private byte[] getMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            return md5.digest(str.getBytes());
        } catch (Throwable e) {
            log.error("Get md5 error", e);
            throw new RuntimeException(e);
        }
    }

    private long hash(byte[] digest, int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[number * 4] & 0xFF))
                & 0xFFFFFFFFL;
    }

}
