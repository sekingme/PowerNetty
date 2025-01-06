 /* power by sekingme */

package org.infraRpcExample.registry.impl.zookeeper;


import com.google.auto.service.AutoService;
import org.infraRpcExample.registry.Registry;
import org.infraRpcExample.registry.RegistryFactory;

/**
 * @author sekingme
 */
@AutoService(RegistryFactory.class)
public final class ZookeeperRegistryFactory implements RegistryFactory {

    @Override
    public String name() {
        return "zookeeper";
    }

    @Override
    public Registry create() {
        return new ZookeeperRegistry();
    }
}
