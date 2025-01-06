package org.remoteRpcExample.codec; /* power by sekingme */

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoStuffSerializer implements Serializer {

    private static final ThreadLocal<LinkedBuffer> localBuffer = ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
//    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) schemaCache.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = getSchema(clazz);
        byte[] data;
        LinkedBuffer buffer = localBuffer.get();
        try {
            data = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
            localBuffer.remove();
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        Schema<T> schema = getSchema(clz);
        T obj = schema.newMessage();
        if (null == obj) {
            return null;
        }
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}
