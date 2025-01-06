 /* power by sekingme */

package org.infraRpcExample.future;

import org.infraRpcExample.listener.SseEmitterDataConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.infraRpcExample.utils.Constants.STREAM_EVENT_DONE;

/**
 * response future
 *
 * @author sekingme
 */
public class StreamResponseFuture {

    private static final Logger logger = LoggerFactory.getLogger(StreamResponseFuture.class);

    private static final ConcurrentHashMap<Long, Consumer<String>> FUTURE_CONSUMER = new ConcurrentHashMap<>(256);

    private static final ConcurrentHashMap<Long, Long> FUTURE_TIMEOUT = new ConcurrentHashMap<>(256);

    public static void putConsumer(long opaque, long timeoutMillis, Consumer<String> dataConsumer) {
        long releaseTime = System.currentTimeMillis() + timeoutMillis + 1000;
        FUTURE_TIMEOUT.put(opaque, releaseTime);
        FUTURE_CONSUMER.put(opaque, dataConsumer);
        logger.info("[Stream Consumer Put]. opaque: {}, releaseTime: {}, size: {}", opaque, releaseTime, FUTURE_CONSUMER.size());
    }

    public static void removeDataConsumer(long opaque) {
        if (FUTURE_CONSUMER.containsKey(opaque)) {
            // 针对自定义SseEmitterDataConsumer类型，执行关闭SseEmitter
            Consumer<String> consumer = FUTURE_CONSUMER.get(opaque);
            if (consumer instanceof SseEmitterDataConsumer) {
                ((SseEmitterDataConsumer) consumer).close();
            }
            FUTURE_CONSUMER.remove(opaque);
            FUTURE_TIMEOUT.remove(opaque);
        }
        logger.info("[Netty stream Consumer Remove]. opaque: {}. size: {}", opaque, FUTURE_CONSUMER.size());
    }

    public static void accept(long opaque, String data) {
        if (FUTURE_CONSUMER.containsKey(opaque)) {
            Consumer<String> consumer = FUTURE_CONSUMER.get(opaque);
            consumer.accept(data);
            if (STREAM_EVENT_DONE.equals(data)) {
                removeDataConsumer(opaque);
            }
        } else {
            logger.error("[Netty stream Consumer Error]. {} is not in {}.", opaque, FUTURE_CONSUMER.keySet());
        }
    }

    /**
     * scan future consumer
     */
    public static void scanFutureConsumer() {
        final List<Long>                opaqueList = new LinkedList<>();
        Iterator<Map.Entry<Long, Long>> it         = FUTURE_TIMEOUT.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> next        = it.next();
            Long                  releaseTime = next.getValue();
            long                  currentTime = System.currentTimeMillis();
            if (releaseTime <= currentTime) {
                Long opaque = next.getKey();
                opaqueList.add(opaque);
                logger.warn("remove timeout opaque : {}, releaseTime: {} < currentTime: {}",
                        opaque, releaseTime, currentTime);
            }
        }
        for (Long opaque : opaqueList) {
            try {
                removeDataConsumer(opaque);
            } catch (Exception ex) {
                logger.warn("scanFutureTable, execute callback error", ex);
            }
        }
    }


}
