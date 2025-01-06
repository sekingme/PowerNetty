package org.infraRpcExample.host.selector;

/**
 * @author: sekingme
 * @description: 1. 轮询 (Round Robin)：将请求均匀分配到所有节点，适合节点负载相近的情况。
 * 2. 加权轮询 (Weighted Round Robin)：根据每个节点的权重进行轮询，权重高的节点会处理更多请求。
 * 3. 最少连接 (Least Connections)：选择当前连接数最少的节点，适合连接时长不一的场景。
 * 4. 随机 (Random)：随机选择一个节点，简单有效，但可能导致不均衡。
 * 5. 哈希 (Hash)：根据请求的某个特征（如 IP 地址）进行哈希运算，选择特定节点，适合需要会话保持的场景。
 * 6. 一致性哈希 (Consistent Hashing)：在节点变动时，减少请求重新分配，提高缓存命中率。
 * @create: 2024-09-25 17:04
 **/
public enum SelectorAlgorithm {
    /**
     * 加权随机算法
     */
    RANDOM,

    /**
     * 轮询算法
     */
    ROUND_ROBIN,

    /**
     * 最小等待任务数
     */
    LOWER_WAITING_TASK,

    /**
     * 一致性哈希算法
     */
    CONSISTENT_HASH;

    // 可增加更多合适算法选择

    public static SelectorAlgorithm of(String selector) {
        for (SelectorAlgorithm hs : values()) {
            if (hs.name().equalsIgnoreCase(selector)) {
                return hs;
            }
        }
        throw new IllegalArgumentException("invalid host selector : " + selector);
    }
}
