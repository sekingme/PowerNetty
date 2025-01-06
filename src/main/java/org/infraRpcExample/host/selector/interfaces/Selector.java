 /* power by sekingme */

package org.infraRpcExample.host.selector.interfaces;

import org.infraRpcExample.protocol.RpcRequest;

import java.util.Collection;


/**
 * selector
 *
 * @param <T> T
 * @author sekingme
 */
public interface Selector<T> {

    /**
     * select
     *
     * @param source  source
     * @param request request
     * @return T
     */
    T select(Collection<T> source, RpcRequest request);
}
