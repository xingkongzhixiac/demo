package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.analysis.*;
import com.example.demo.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    private final AnalysisService analysisService;

    @GetMapping("/matrix")
    public ApiResponse<List<SalaryMatrixDto>> getMatrix(JobSearchRequest request) {
        try {
            return ApiResponse.success(analysisService.getSalaryMatrix(request));
        } catch (Exception e) {
            logger.error("Error generating Salary Matrix", e);
            return ApiResponse.error(500, "Error generating analysis data");
        }
    }

    @GetMapping("/scatter")
    public ApiResponse<List<ScatterPlotDto>> getScatter(JobSearchRequest request) {
        try {
            return ApiResponse.success(analysisService.getScatterPlot(request));
        } catch (Exception e) {
            logger.error("Error generating Scatter Plot", e);
            return ApiResponse.error(500, "Error generating analysis data");
        }
    }

    @GetMapping("/wordcloud")
    public ApiResponse<List<WordCloudDto>> getWordCloud(JobSearchRequest request) {
        try {
            return ApiResponse.success(analysisService.getWordCloud(request));
        } catch (Exception e) {
            logger.error("Error generating Word Cloud", e);
            return ApiResponse.error(500, "Error generating analysis data");
        }
    }

    @GetMapping("/network")
    public ApiResponse<NetworkGraphDto> getNetwork(JobSearchRequest request) {
        try {
            return ApiResponse.success(analysisService.getNetworkGraph(request));
        } catch (Exception e) {
            logger.error("Error generating Network Graph", e);
            return ApiResponse.error(500, "Error generating analysis data");
        }
    }
}