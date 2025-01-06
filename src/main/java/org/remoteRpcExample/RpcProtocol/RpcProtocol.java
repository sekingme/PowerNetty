 /* power by sekingme */

package org.remoteRpcExample.RpcProtocol;

/**
 * @author Sekingme
 */
public class RpcProtocol<T> {

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
