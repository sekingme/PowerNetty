 /* power by sekingme */
package org.infraRpcExample.exceptions;

/**
 * too much request exception
 */
public class RemotingTooMuchRequestException extends RemotingException {

    public RemotingTooMuchRequestException(String message) {
        super(message);
    }
}
