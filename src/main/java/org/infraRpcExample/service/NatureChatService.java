package org.infraRpcExample.service;

import org.infraRpcExample.request.PowerSqlRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NatureChatService {

    SseEmitter natureChatEmitter(PowerSqlRequest chatRequest);
}
