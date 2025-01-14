 /* power by sekingme */

package org.infraRpcExample.exceptions;


/**
 * timeout exception
 */
public class RemotingTimeoutException extends RemotingException {

    public RemotingTimeoutException(String message) {
        super(message);
    }


    public RemotingTimeoutException(String address, long timeoutMillis) {
        this(address, timeoutMillis, null);
    }

    public RemotingTimeoutException(String address, long timeoutMillis, Throwable cause) {
        super(String.format("wait response on the channel %s timeout %s", address, timeoutMillis), cause);
    }
}
