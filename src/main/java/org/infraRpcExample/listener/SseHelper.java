package org.infraRpcExample.listener;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE（Sever-Sent Event），就是浏览器向服务器发送一个HTTP请求，保持长连接，服务器不断单向地向浏览器推送“信息”（message）
 * <p>
 * For Test
 *
 * @author sekingme change
 */
@UtilityClass
@Slf4j
public class SseHelper {

    public void complete(SseEmitter sseEmitter) {
        try {
            sseEmitter.complete();
        } catch (Exception e) {
            log.error("SseEmitter complete error.", e);
        }
    }

    public void send(SseEmitter sseEmitter, Object data) {
        try {
            sseEmitter.send(data, MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            log.error("SseEmitter send error.", e);
        }
    }
}
