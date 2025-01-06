package org.infraRpcExample.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * @author sekingme change
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    /**
     * 目前支持三中角色参考官网，进行情景输入：https://platform.openai.com/docs/guides/chat/introduction
     * Azure openai同样如此
     */
    private String role;
    private String content;

    public static Message ofUser(String content) {

        return new Message(Role.USER.getValue(), content);
    }

    public static Message ofSystem(String content) {

        return new Message(Role.SYSTEM.getValue(), content);
    }

    public static Message ofAssistant(String content) {

        return new Message(Role.ASSISTANT.getValue(), content);
    }

    /**
     * 最新的chat completion接口支持三种角色，避免旧的completion接口需要连接会话提示模型
     */
    @Getter
    @AllArgsConstructor
    public enum Role {

        /**
         * 系统角色设定，比如设定系统为一个分析师
         */
        SYSTEM("system"),
        /**
         * 提问的prompt
         */
        USER("user"),
        /**
         * 上下文内容
         */
        ASSISTANT("assistant"),
        ;

        private String value;
    }

}
