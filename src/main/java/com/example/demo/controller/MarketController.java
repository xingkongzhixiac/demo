package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.market.*;
import com.example.demo.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);
    private final MarketService marketService;

    @GetMapping("/heat")
    public ApiResponse<List<HeatMapDto>> getHeatMap(JobSearchRequest request) {
        try {
            return ApiResponse.success(marketService.getHeatMapData(request));
        } catch (Exception e) {
            logger.error("Error generating HeatMap data", e);
            return ApiResponse.error(500, "Failed to load heat map data");
        }
    }

    @GetMapping("/tech")
    public ApiResponse<List<NameValueDto>> getTechStack(JobSearchRequest request) {
        try {
            return ApiResponse.success(marketService.getTopTechStack(request));
        } catch (Exception e) {
            logger.error("Error generating Tech Stack data", e);
            return ApiResponse.error(500, "Failed to load tech stack data");
        }
    }

    @GetMapping("/finance")
    public ApiResponse<List<NameValueDto>> getFinance(JobSearchRequest request) {
        try {
            return ApiResponse.success(marketService.getFinanceDistribution(request));
        } catch (Exception e) {
            logger.error("Error generating Finance data", e);
            return ApiResponse.error(500, "Failed to load finance data");
        }
    }

    @GetMapping("/salary-trend")
    public ApiResponse<List<TrendDto>> getSalaryTrend(JobSearchRequest request) {
        try {
            return ApiResponse.success(marketService.getSalaryTrend(request));
        } catch (Exception e) {
            logger.error("Error generating Salary Trend data", e);
            return ApiResponse.error(500, "Failed to load salary trend data");
        }
    }

    @GetMapping("/radar")
    public ApiResponse<List<RadarDto>> getRadar(JobSearchRequest request) {
        try {
            return ApiResponse.success(marketService.getAbilityRadar(request));
        } catch (Exception e) {
            logger.error("Error generating Radar data", e);
            return ApiResponse.error(500, "Failed to load radar data");
        }
    }
}