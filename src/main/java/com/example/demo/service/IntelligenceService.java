package com.example.demo.service;

import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.intelligence.*;
import com.example.demo.model.LagouData;
import com.example.demo.repository.LagouDataRepository;
import com.example.demo.util.SalaryUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntelligenceService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligenceService.class);
    private static final int MAX_ANALYSIS_ROWS = 5000;

    // 原有的依赖
    private final LagouDataRepository lagouDataRepository;
    private final DynamicQueryService dynamicQueryService;

    // 新增：HTTP 请求工具
    private final RestTemplate restTemplate;

    // 新增：读取配置文件中的 AI 参数
    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String aiApiKey;

    @Value("${ai.api.model}")
    private String aiApiModel;

    // --- 辅助方法：获取筛选数据 (保持不变) ---
    private List<LagouData> getFilteredData(JobSearchRequest request) {
        Specification<LagouData> spec = dynamicQueryService.buildSpecification(request);
        Pageable limit = PageRequest.of(0, MAX_ANALYSIS_ROWS);
        List<LagouData> rawData = lagouDataRepository.findAll(spec, limit).getContent();

        if (request.getSalaryRange() == null || request.getSalaryRange().size() != 2) {
            return rawData;
        }

        int minFilter = request.getSalaryRange().get(0);
        int maxFilter = request.getSalaryRange().get(1);

        if (minFilter <= 0 && maxFilter >= 100) {
            return rawData;
        }

        return rawData.stream().filter(job -> {
            Double salary = SalaryUtil.parseSalary(job.getSalary());
            return salary > 0 && salary >= minFilter && salary <= maxFilter;
        }).collect(Collectors.toList());
    }

    // --- 1. 薪资分布 (箱线图) (保持不变) ---
    public List<BoxPlotDto> getSalaryBoxPlot(JobSearchRequest request) {
        List<LagouData> allJobs = getFilteredData(request);

        Map<String, List<Double>> industrySalaries = new HashMap<>();
        for (LagouData job : allJobs) {
            String industryRaw = job.getIndustryField();
            Double salary = SalaryUtil.parseSalary(job.getSalary());

            if (industryRaw != null && !industryRaw.isEmpty() && salary > 0) {
                String[] industries = industryRaw.split("[,，]");
                for (String ind : industries) {
                    industrySalaries.computeIfAbsent(ind.trim(), k -> new ArrayList<>()).add(salary);
                }
            }
        }

        List<BoxPlotDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : industrySalaries.entrySet()) {
            List<Double> salaries = entry.getValue();
            if (salaries.size() < 10) continue;

            Collections.sort(salaries);

            double min = salaries.get(0);
            double max = salaries.get(salaries.size() - 1);
            double q1 = getPercentile(salaries, 0.25);
            double median = getPercentile(salaries, 0.50);
            double q3 = getPercentile(salaries, 0.75);

            List<Double> values = Arrays.asList(min, q1, median, q3, max);
            result.add(new BoxPlotDto(entry.getKey(), values));
        }

        return result.stream()
                .filter(dto -> dto.getValues().size() == 5)
                .sorted((a, b) -> Double.compare(b.getValues().get(2), a.getValues().get(2)))
                .limit(15)
                .collect(Collectors.toList());
    }

    // --- 2. 市场层级 (旭日图) (保持不变) ---
    public List<SunburstDto> getMarketHierarchy(JobSearchRequest request) {
        List<LagouData> allJobs = getFilteredData(request);

        Map<String, Map<String, Long>> cityGroup = allJobs.stream()
                .filter(j -> j.getCity() != null && j.getDistrict() != null && !j.getCity().isEmpty() && !j.getDistrict().isEmpty())
                .collect(Collectors.groupingBy(
                        LagouData::getCity,
                        Collectors.groupingBy(LagouData::getDistrict, Collectors.counting())
                ));

        List<SunburstDto> rootChildren = new ArrayList<>();

        for (Map.Entry<String, Map<String, Long>> cityEntry : cityGroup.entrySet()) {
            String cityName = cityEntry.getKey();
            List<SunburstDto> districtNodes = new ArrayList<>();
            double cityTotal = 0.0;

            for (Map.Entry<String, Long> districtEntry : cityEntry.getValue().entrySet()) {
                double val = districtEntry.getValue().doubleValue();
                cityTotal += val;
                // 注意：使用 4 参数构造函数 (name, value, children, itemStyle)
                districtNodes.add(new SunburstDto(districtEntry.getKey(), val, null, null));
            }

            districtNodes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            rootChildren.add(new SunburstDto(cityName, cityTotal, districtNodes, null));
        }

        rootChildren.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return rootChildren.stream().limit(5).collect(Collectors.toList());
    }

    // --- 3. AI 对话逻辑 (已切换为真实 API 调用) ---
    public String chatWithAI(String userMessage) {
        // 1. 基础校验
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "请输入您想了解的招聘信息。";
        }

        try {
            // 2. 设置 HTTP 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);

            // 3. 构建 Prompt (提示词)
            // System Role: 设定 AI 的人设，让它知道自己是在做招聘数据分析
            String systemPrompt = "你是一个专业的数据分析师助手，服务于一个招聘数据可视化大屏。" +
                    "请根据用户的提问，结合通用的互联网招聘市场知识进行简明扼要的回答。" +
                    "回答风格要专业、客观，字数控制在150字以内。";

            List<OpenAiRequest.Message> messages = new ArrayList<>();
            messages.add(OpenAiRequest.Message.builder().role("system").content(systemPrompt).build());
            messages.add(OpenAiRequest.Message.builder().role("user").content(userMessage).build());

            // 4. 构建请求体
            OpenAiRequest requestBody = OpenAiRequest.builder()
                    .model(aiApiModel)
                    .messages(messages)
                    .temperature(0.6) // 控制回答的创造性
//                    .stream(false)
                    .build();

            HttpEntity<OpenAiRequest> entity = new HttpEntity<>(requestBody, headers);

            // 5. 发起 POST 请求
            ResponseEntity<OpenAiResponse> response = restTemplate.postForEntity(
                    aiApiUrl,
                    entity,
                    OpenAiResponse.class
            );

            // 6. 解析并返回结果
            if (response.getBody() != null &&
                    response.getBody().getChoices() != null &&
                    !response.getBody().getChoices().isEmpty()) {

                return response.getBody().getChoices().get(0).getMessage().getContent();
            }

            return "AI 服务暂无响应，请稍后再试。";

        } catch (Exception e) {
            logger.error("AI API Call Failed", e);
            // 可以在这里根据异常类型返回更友好的提示，比如 "Key过期" 或 "网络超时"
            return "连接 AI 服务时出现问题: " + e.getMessage();
        }
    }

    // 辅助方法 (保持不变)
    private double getPercentile(List<Double> sortedData, double percentile) {
        if (sortedData.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile * sortedData.size()) - 1;
        if (index < 0) index = 0;
        return sortedData.get(index);
    }
}