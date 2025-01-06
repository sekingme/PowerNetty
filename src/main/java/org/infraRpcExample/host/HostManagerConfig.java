 /* power by sekingme */

package org.infraRpcExample.host;

import org.infraRpcExample.host.hostmanager.ConsistentHashHostManager;
import org.infraRpcExample.host.hostmanager.LowerWaitingTaskHostManager;
import org.infraRpcExample.host.hostmanager.RandomHostManager;
import org.infraRpcExample.host.hostmanager.RoundRobinHostManager;
import org.infraRpcExample.host.hostmanager.interfaces.HostManager;
import org.infraRpcExample.host.selector.SelectorAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: sekingme
 * @description: host manager config: determine selector algorithm(read from apollo) in HostManager when spring app start
 * @create: 2024-09-25 17:14
 */
@Configuration
public class HostManagerConfig {

    private AutowireCapableBeanFactory beanFactory;

    @Value("${netty.host.selector.algorithm:RANDOM}")
    private String hostSelector;

    @Autowired
    public HostManagerConfig(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public HostManager hostManager() {
        SelectorAlgorithm selector = SelectorAlgorithm.of(hostSelector);
        HostManager       hostManager;
        switch (selector) {
            case RANDOM:
                hostManager = new RandomHostManager();
                break;
            case ROUND_ROBIN:
                hostManager = new RoundRobinHostManager();
                break;
            case LOWER_WAITING_TASK:
                hostManager = new LowerWaitingTaskHostManager();
                break;
            case CONSISTENT_HASH:
                hostManager = new ConsistentHashHostManager();
                break;
            default:
                throw new IllegalArgumentException("unSupport selector algorithm until now: " + hostSelector);
        }
        beanFactory.autowireBean(hostManager);
        return hostManager;
    }
}
