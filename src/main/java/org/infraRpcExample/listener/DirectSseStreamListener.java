package org.infraRpcExample.listener;

import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.infraRpcExample.request.PowerSqlException;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.util.Objects;

import static org.infraRpcExample.utils.Constants.STREAM_EVENT_DONE;
import static org.infraRpcExample.utils.Constants.STREAM_EVENT_ERROR;

/**
 * @author Colin
 * @description:
 * @date 2024/5/24 5:48 PM
 */
@RequiredArgsConstructor
public class DirectSseStreamListener extends AbstractStreamListener {

    final SseEmitter sseEmitter;
    private final Logger logger = LoggerFactory.getLogger(DirectSseStreamListener.class);

    @Override
    public void onClosed(EventSource eventSource) {
        sseEmitter.complete();
    }

    @Override
    public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
        try {
//            logger.info("onEvent data: {}", data);
            if (StringUtils.isNotEmpty(data) && data.startsWith(STREAM_EVENT_ERROR)) {
                sseEmitter.send(data, MediaType.TEXT_PLAIN);
                sseEmitter.send(STREAM_EVENT_DONE, MediaType.TEXT_PLAIN);
                sseEmitter.complete();
            } else {
                sseEmitter.send(data, MediaType.TEXT_PLAIN);
            }
        } catch (Exception e) {
            logger.error("SseEmitter send error.", e);
        }
    }


    @Override
    public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        try {
            String errorMessage = "";
            if (Objects.nonNull(response) && Objects.nonNull(response.body())) {
                try {
                    PowerSqlException powerSqlException = ObjectMapperConfig.parseCommonObject(response.body().string(), PowerSqlException.class);
                    errorMessage = powerSqlException.getMessage();
                } catch (Exception e) {
                    errorMessage = "未知异常！";
                }
            } else {
                errorMessage = "未知异常！";
            }
            logger.error("response：{}", errorMessage);
            try {
                SseHelper.send(sseEmitter, STREAM_EVENT_ERROR + errorMessage);
                SseHelper.send(sseEmitter, STREAM_EVENT_DONE);
            } catch (Exception e) {
                logger.error("SseEmitter send error.", e);
            }
        } catch (Exception e) {
            logger.error("Failed to read response body", e);
        } finally {
            onClosed(eventSource);
        }
    }

    @Override
    public void onMsg(String message) {
        logger.info("onMsg message: {}", message);
        if (StringUtils.isNotEmpty(message) && message.startsWith(STREAM_EVENT_ERROR)) {
            SseHelper.send(sseEmitter, message);
            SseHelper.send(sseEmitter, STREAM_EVENT_DONE);
            sseEmitter.complete();
        } else {
            SseHelper.send(sseEmitter, message);
        }
    }

    @Override
    public void onError(Throwable throwable, String response) {

    }
}
