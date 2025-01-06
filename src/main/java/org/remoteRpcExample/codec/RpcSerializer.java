package org.remoteRpcExample.codec; /* power by sekingme */

import java.util.HashMap;

/**
 * @author Sekingme
 */

public enum RpcSerializer {


    PROTOSTUFF((byte) 1, new ProtoStuffSerializer());

    byte type;

    Serializer serializer;

    RpcSerializer(byte type, Serializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public byte getType() {
        return type;
    }

    private static HashMap<Byte, Serializer> SERIALIZERS_MAP = new HashMap<>();

    static {
        for (RpcSerializer rpcSerializer : RpcSerializer.values()) {
            SERIALIZERS_MAP.put(rpcSerializer.type, rpcSerializer.serializer);
        }
    }

    public static Serializer getSerializerByType(byte type) {
        return SERIALIZERS_MAP.get(type);
    }
}
