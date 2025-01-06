package org.infraRpcExample.service.impl;

import cn.hutool.http.ContentType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.infraRpcExample.listener.DirectSseStreamListener;
import org.infraRpcExample.request.PowerSqlRequest;
import org.infraRpcExample.service.NatureChatService;
import org.infraRpcExample.utils.ObjectMapperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NatureChatServiceImpl implements NatureChatService {

    private final Logger logger = LoggerFactory.getLogger(NatureChatServiceImpl.class);

    private String natureChatUrl = "http://test-powersql.bigdata.bigo.inner/api/natureChat/chat";

    @Override
    public SseEmitter natureChatEmitter(PowerSqlRequest chatRequest) {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        try {
            OkHttpClient            okHttpClient = new OkHttpClient();
            DirectSseStreamListener listener     = new DirectSseStreamListener(sseEmitter);
            EventSource.Factory     factory      = EventSources.createFactory(okHttpClient);

            String requestBody = ObjectMapperConfig.getSnakeObjectMapper().writeValueAsString(chatRequest);

            Request request = new Request.Builder()
                    .url(natureChatUrl)
                    .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse(ContentType.JSON.getValue()),
                            requestBody))
                    .build();
            factory.newEventSource(request, listener);
            return sseEmitter;
        } catch (Exception e) {
            logger.error("natureChat fail, request: {}", ObjectMapperConfig.toCommonJsonString(chatRequest), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
