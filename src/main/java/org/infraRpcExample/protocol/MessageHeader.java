 /* power by sekingme */

package org.infraRpcExample.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sekingme
 */
public class MessageHeader {

    // 用于核对编解码数据是否一致
    public static final  byte       MAGIC      = (byte) 0xbabe;
    private static final AtomicLong REQUEST_ID = new AtomicLong(1);
    /**
     * 请求类型
     */
    private byte eventType;
    /**
     * 消息体长度
     */
    private int msgLength = 0;
    /**
     * 请求的ID
     */
    private long opaque;
    /**
     * web服务和旧DP服务中动态带上用户名和工号信息，其它服务静态值
     */
    private byte userName;
    private byte userWorkId;
    public MessageHeader() {
        this.opaque = REQUEST_ID.getAndIncrement();
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

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
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
