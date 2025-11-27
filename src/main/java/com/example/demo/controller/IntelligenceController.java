package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.intelligence.*;
import com.example.demo.service.IntelligenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private static final Logger logger = LoggerFactory.getLogger(IntelligenceController.class);
    private final IntelligenceService intelligenceService;

    @GetMapping("/salary-dist")
    public ApiResponse<List<BoxPlotDto>> getSalaryDistribution(JobSearchRequest request) {
        try {
            return ApiResponse.success(intelligenceService.getSalaryBoxPlot(request));
        } catch (Exception e) {
            logger.error("Error generating Salary Distribution", e);
            return ApiResponse.error(500, "Failed to load salary data");
        }
    }

    @GetMapping("/market-hierarchy")
    public ApiResponse<List<SunburstDto>> getMarketHierarchy(JobSearchRequest request) {
        try {
            return ApiResponse.success(intelligenceService.getMarketHierarchy(request));
        } catch (Exception e) {
            logger.error("Error generating Market Hierarchy", e);
            return ApiResponse.error(500, "Failed to load hierarchy data");
        }
    }

    // 3. AI 对话接口 (真实接口)
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody ChatRequest chatRequest) {
        try {
            // 真实 AI 接口调用不需要模拟延迟
            String response = intelligenceService.chatWithAI(chatRequest.getMessage());
            return ApiResponse.success(response);
        } catch (Exception e) {
            logger.error("Error during AI chat", e);
            // 返回 500 或者 200 带错误提示均可，这里保持一致性
            return ApiResponse.error(500, "AI Service Unavailable");
        }
    }
}