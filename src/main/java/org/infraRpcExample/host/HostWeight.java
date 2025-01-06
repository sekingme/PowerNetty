 /* power by sekingme */

package org.infraRpcExample.host;

import lombok.Data;
import org.infraRpcExample.utils.Constants;

/**
 * host weight
 *
 * @author sekingme
 */
@Data
public class HostWeight {

    private final int CPU_FACTOR = 10;

    private final int MEMORY_FACTOR = 20;

    private final int LOAD_AVERAGE_FACTOR = 70;

    private final int weight;

    private final int waitingTaskCount;

    public HostWeight(double cpu, double memory, double loadAverage, int waitingTaskCount, long startTime) {
        this.weight = Double.valueOf(calculateWeight(cpu, memory, loadAverage, startTime)).intValue();
        this.waitingTaskCount = waitingTaskCount;
    }

    @Override
    public String toString() {
        return "HostWeight{"
                + "weight=" + weight
                + ", waitingTaskCount=" + waitingTaskCount
                + '}';
    }

    private double calculateWeight(double cpu, double memory, double loadAverage, long startTime) {
        double calculatedWeight = cpu * CPU_FACTOR + memory * MEMORY_FACTOR + loadAverage * LOAD_AVERAGE_FACTOR;
        calculatedWeight = 100.0 - calculatedWeight;
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime > 0 && uptime < Constants.WARM_UP_TIME) {
            // 如果启动时间小于预热时间，按比例缩减权重
            return calculatedWeight * uptime / Constants.WARM_UP_TIME;
        }
        return calculatedWeight;
    }

}
