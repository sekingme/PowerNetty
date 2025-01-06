 /* power by sekingme */

package org.infraRpcExample.protocol;

/**
 * @author Sekingme
 */

public enum EventType {

    HEARTBEAT((byte) 1, "heartbeat"),
    BATCH_REQUEST((byte) 2, "batch request"),
    STREAM_REQUEST((byte) 3, "stream request"),
    RESPONSE((byte) 4, "response");

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
