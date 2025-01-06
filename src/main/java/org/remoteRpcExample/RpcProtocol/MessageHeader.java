 /* power by sekingme */

package org.remoteRpcExample.RpcProtocol;

/**
 * @author Sekingme
 */
public class MessageHeader {

    private byte version = 1;

    private byte eventType;

    private int msgLength = 0;

    private long requestId = 0L;

    private byte serialization = 0;

    private short magic = RpcProtocolConstants.MAGIC;

    // web服务中动态带上用户名和工号信息，其它服务设置静态值
    private byte userName;

    private byte userWorkId;

    public short getMagic() {
        return magic;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public byte getSerialization() {
        return serialization;
    }

    public void setSerialization(byte serialization) {
        this.serialization = serialization;
    }

    public byte getUserName() {
        return userName;
    }

    public void setUserName(byte userName) {
        this.userName = userName;
    }

    public byte getUserWorkId() {
        return userWorkId;
    }

    public void setUserWorkId(byte userWorkId) {
        this.userWorkId = userWorkId;
    }
}
