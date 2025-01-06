 /* power by sekingme */

package org.infraRpcExample.host;


import lombok.Data;
import org.infraRpcExample.enums.NodeType;

/**
 * @author: sekingme
 * @description: 在host的基础上增加权重和host类型信息
 * @create: 2024-09-25 17:23
 */
@Data
public class HostWorker extends Host {

    /**
     * host weight
     */
    private HostWeight hostWeight;

    /**
     * worker type
     */
    private NodeType nodeType;

    public HostWorker(String ip, int port, HostWeight hostWeight, NodeType nodeType) {
        super(ip, port);
        this.hostWeight = hostWeight;
        this.nodeType = nodeType;
    }

    public HostWorker(String address, HostWeight hostWeight, NodeType nodeType) {
        super(address);
        this.hostWeight = hostWeight;
        this.nodeType = nodeType;
    }

    public static HostWorker of(String address, HostWeight hostWeight, NodeType nodeType) {
        return new HostWorker(address, hostWeight, nodeType);
    }

    public int getWeight() {
        return hostWeight.getWeight();
    }

    @Override
    public String toString() {
        return "Host{"
                + "address='" + getAddress() + '\''
                + ", hostWeight=" + hostWeight
                + ", NodeType='" + nodeType.getServerName() + '\''
                + '}';
    }

}
