package com.example.demo.dto.intelligence;

import lombok.Data;
import java.util.List;

/**
 * AI 接口返回的响应体结构
 */
@Data
public class OpenAiResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private OpenAiRequest.Message message;
    }
}