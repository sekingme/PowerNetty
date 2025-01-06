package org.infraRpcExample.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * chat答案类
 *
 * @author sekingme change
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {
    private String           id;
    private String           object;
    private long             created;
    private String           model;
    /**
     * 响应的答案在choices中
     */
    private List<ChatChoice> choices;
}
