 /* power by sekingme */

package org.infraRpcExample.thread;

/**
 * server stop interface.
 */
public interface IStoppable {

    /**
     * Stop this service.
     *
     * @param cause why stopping
     */
    void stop(String cause);

}
