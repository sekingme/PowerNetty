package org.infraRpcExample.codec; /* power by sekingme */

import java.util.HashMap;

public enum RpcSerializer {


    PROTOSTUFF((byte) 1, new ProtoStuffSerializer()),
    KRYO((byte) 4, new KryoSerializer()),
    ;

    private static HashMap<Byte, Serializer> SERIALIZERS_MAP = new HashMap<>();

    static {
        for (RpcSerializer rpcSerializer : RpcSerializer.values()) {
            SERIALIZERS_MAP.put(rpcSerializer.type, rpcSerializer.serializer);
        }
    }

    byte type;
    Serializer serializer;

    RpcSerializer(byte type, Serializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public static Serializer getSerializerByType(byte type) {
        return SERIALIZERS_MAP.get(type);
    }

    public byte getType() {
        return type;
    }
}
