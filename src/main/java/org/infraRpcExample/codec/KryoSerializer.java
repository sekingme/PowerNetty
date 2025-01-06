package org.infraRpcExample.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author: Sekingme
 * @description:
 * @create: 2024-10-29 20:37
 **/
public class KryoSerializer implements Serializer {
    private final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);
    private final Kryo kryo;

    public KryoSerializer() {
        kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        registerAliases(kryo);
    }

    private void registerAliases(Kryo kryo) {
        // 注册类及其别名
        try {
//            kryo.register(Class.forName("sg.bigo.datapower.rbac.model.application.ApplicationEventRequest"), 1);
//            kryo.register(Class.forName("sg.bigo.datapower.rbac.model.application.AppUser"), 2);
        } catch (Exception e) {
            logger.error("Failed to register some class for KryoSerializer.", e);
        }
    }

    @Override
    public <T> byte[] serialize(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            kryo.writeClassAndObject(output, obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            logger.error("KryoSerializer serialize failed", e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            Input input = new Input(byteArrayInputStream)) {
            return (T)kryo.readClassAndObject(input);
        } catch (Exception e) {
            logger.error("KryoSerializer deserialization failed", e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}
