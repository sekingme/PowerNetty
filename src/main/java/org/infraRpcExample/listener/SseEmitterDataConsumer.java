package org.infraRpcExample.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

import static org.infraRpcExample.utils.Constants.STREAM_EVENT_DONE;

/**
 * @author zhushuhao
 * @description: SeeEmitter Consumer
 * @date 2024/10-14 16:55
 */
public class SseEmitterDataConsumer implements Consumer<String> {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitterDataConsumer.class);

    private final SseEmitter sseEmitter;

    public SseEmitterDataConsumer(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void accept(String data) {
        try {
            sseEmitter.send(data);
            if (data.equals(STREAM_EVENT_DONE)) {
                sseEmitter.complete();
            }
        } catch (IOException e) {
            logger.error("IOException:", e);
        }
    }

    public void close() {
        try {
            sseEmitter.complete();
        } catch (Exception e) {
            logger.error("Exception: Failed to complete SseEmitter.", e);
        }
    }
}
