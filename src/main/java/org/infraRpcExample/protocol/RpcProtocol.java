 /* power by sekingme */

package org.infraRpcExample.protocol;

import java.io.Serializable;

/**
 * @author sekingme
 */
public class RpcProtocol<T> implements Serializable {

    private MessageHeader msgHeader;

    private T body;

    public MessageHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MessageHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
