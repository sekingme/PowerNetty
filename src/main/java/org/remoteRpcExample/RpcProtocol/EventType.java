 /* power by sekingme */

package org.remoteRpcExample.RpcProtocol;

/**
 * @author Sekingme
 */

public enum EventType {

    HEARTBEAT((byte)1, "heartbeat"),
    REQUEST((byte)2, "business request"),
    RESPONSE((byte)3, "business response");

    private byte type;

    private String description;

    EventType(byte type, String description) {
        this.type = type;
        this.description = description;
    }

    public byte getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
