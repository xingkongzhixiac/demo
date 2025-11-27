package com.example.demo.dto.intelligence;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 发送给 AI 接口的请求体结构
 */
@Data
@Builder
public class OpenAiRequest {
    private String model;
    private List<Message> messages;
    private Double temperature; // 0.0 ~ 1.0，值越高回答越随机

    @Data
    @Builder
    public static class Message {
        private String role; // system, user, assistant
        private String content;
    }
}