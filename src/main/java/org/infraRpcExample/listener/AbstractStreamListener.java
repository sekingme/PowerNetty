package org.infraRpcExample.listener;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.infraRpcExample.request.PowerSqlException;
import org.infraRpcExample.response.ChatChoice;
import org.infraRpcExample.response.ChatCompletionResponse;
import org.infraRpcExample.response.Message;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.infraRpcExample.utils.Constants.STREAM_EVENT_DONE;
import static org.infraRpcExample.utils.Constants.STREAM_EVENT_ERROR;

/**
 * EventSource listener for chat-related events.
 * For Test
 *
 * @author sekingme change
 */
public abstract class AbstractStreamListener extends EventSourceListener {

    private final Logger logger = LoggerFactory.getLogger(AbstractStreamListener.class);

    protected String lastMessage = "";


    /**
     * Called when all new message are received.
     *
     * @param message the new message
     */
    @Setter
    @Getter
    protected Consumer<String> onComplate = s -> {
        // 接受到[DONE]时调用，可用于在[DONE]前插入{"created":0,"messageId":2522,"type":"ID"}
    };

    /**
     * Called when a new message is received.
     * 收到消息 单个字
     *
     * @param message the new message
     */
    public abstract void onMsg(String message);

    /**
     * Called when an error occurs.
     * 出错时调用
     *
     * @param throwable the throwable that caused the error
     * @param response  the response associated with the error, if any
     */
    public abstract void onError(Throwable throwable, String response);

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        // do nothing
    }

    @Override
    public void onClosed(EventSource eventSource) {
        // do nothing
        return;
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (data.equals(STREAM_EVENT_DONE)) {
            onComplate.accept(lastMessage);
        } else {

            ChatCompletionResponse response = ObjectMapperConfig.parseCommonObject(data, ChatCompletionResponse.class);
            // 读取Json
            List<ChatChoice> choices = response.getChoices();
            if (choices == null || choices.isEmpty()) {
                return;
            }
            Message delta = choices.get(0).getDelta();
            String  text  = delta.getContent();

            if (text != null) {
                lastMessage += text;
            }
            onMsg(data);
        }
    }


    @SneakyThrows
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
                onMsg(STREAM_EVENT_ERROR + errorMessage);
            } catch (Exception e) {
                logger.error("SseEmitter send error.", e);
            }
        } catch (Exception e) {
            logger.error("Failed to read response body", e);
        } finally {
            onClosed(eventSource);
        }
    }
}
