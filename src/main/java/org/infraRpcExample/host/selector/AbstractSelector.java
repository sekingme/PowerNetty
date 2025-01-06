 /* power by sekingme */
package org.infraRpcExample.host.selector;

import org.apache.commons.collections.CollectionUtils;
import org.infraRpcExample.host.selector.interfaces.Selector;
import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;

/**
 * AbstractSelector 负载均衡抽象类
 *
 * @author sekingme
 */
public abstract class AbstractSelector<T> implements Selector<T> {
    @Override
    public T select(Collection<T> source, RpcRequest request) {

        if (CollectionUtils.isEmpty(source)) {
            throw new IllegalArgumentException("Empty source.");
        }

        // if only one , return directly
        if (source.size() == 1) {
            return (T) source.toArray()[0];
        }
        return doSelect(source, request);
    }

    protected abstract T doSelect(Collection<T> source, RpcRequest request);

}
