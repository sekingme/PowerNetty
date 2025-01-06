package org.infraRpcExample.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.infraRpcExample.response.MessageType;

import java.util.List;
import java.util.Map;

/**
 * @author sekingme
 * Time: 2024/06/11 15:06
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PowerSqlRequest {
    /**
     * AI接口公共字段：AI接口都可以定义同名的字段传输数据。但如有额外字段需求，请通过externInfo传输
     */

    //必填
    private String        chatId;
    private Integer       agentId;
    private Long          messageId;
    private String        model;
    private List<Message> messages;
    private Boolean       isSave = Boolean.TRUE;

    //非必填，自动在DP內部获取
    private String username;
    private String userWorkId;
    private String appLineName;
    private Long   appLineId;

    //非必填，PowerSQL存在默认值或获取
    private Boolean stream;
    private Double  temperature;
    private Integer maxTokens;
    private String  title;

    /**
     * 除上述公共字段外，其它信息请通过externInfo传输。涉及的key请补充说明：
     * tableInfo:           List<String> 非必填
     * refine:              Boolean      必填
     * decompose:           Boolean      必填
     * contextSize:         Integer      必填
     * contextCutPosition:  Integer      必填
     * exceptionSql:        String       非必填
     */
    private Map<String, Object> externInfo;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {

        // 商量后前端不用传输这个字段数据，后端默认置为TEXT
        private MessageType type = MessageType.TEXT;

        private String role;

        private String content;

        private Map<String, Object> externInfo;
    }

}
