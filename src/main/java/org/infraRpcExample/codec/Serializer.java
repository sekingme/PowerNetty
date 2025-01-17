 /* power by sekingme */

package org.infraRpcExample.codec;

import java.io.IOException;

public interface Serializer {

    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;

}
