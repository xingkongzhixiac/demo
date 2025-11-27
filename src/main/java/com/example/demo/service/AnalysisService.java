package com.example.demo.service;

import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.analysis.*;
import com.example.demo.model.LagouData;
import com.example.demo.repository.LagouDataRepository;
import com.example.demo.util.SalaryUtil;
import com.example.demo.util.TextAnalysisUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    private final LagouDataRepository lagouDataRepository;
    private final DynamicQueryService dynamicQueryService;

    // 限制用于分析的最大数据行数，防止 OOM
    private static final int MAX_ANALYSIS_ROWS = 5000;

    // 网络图停用词
    private static final List<String> STOP_WORDS = Arrays.asList(
            "熟悉", "精通", "了解", "开发", "使用", "相关", "工作", "经验", "优先", "能力", "负责", "以及", "具有"
    );

    /**
     * 通用数据获取方法
     */
    private List<LagouData> getFilteredData(JobSearchRequest request) {
        Specification<LagouData> spec = dynamicQueryService.buildSpecification(request);

        // 分页获取前 MAX_ANALYSIS_ROWS 条数据，保护内存
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
            return salary >= minFilter && salary <= maxFilter;
        }).collect(Collectors.toList());
    }

    /**
     * 1. 薪资矩阵热力图
     */
    public List<SalaryMatrixDto> getSalaryMatrix(JobSearchRequest request) {
        List<LagouData> allJobs = getFilteredData(request);
        Map<String, Map<String, List<Double>>> matrixMap = new HashMap<>();

        for (LagouData job : allJobs) {
            String industry = getMainIndustry(job.getIndustryField());
            String workYear = job.getWorkYear();
            Double salary = SalaryUtil.parseSalary(job.getSalary());

            if (industry != null && !industry.isEmpty() && workYear != null && salary > 0) {
                matrixMap.computeIfAbsent(industry, k -> new HashMap<>())
                        .computeIfAbsent(workYear, k -> new ArrayList<>())
                        .add(salary);
            }
        }

        return matrixMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    long count1 = e1.getValue().values().stream().mapToLong(List::size).sum();
                    long count2 = e2.getValue().values().stream().mapToLong(List::size).sum();
                    return Long.compare(count2, count1);
                })
                .limit(10)
                .flatMap(entry -> {
                    String ind = entry.getKey();
                    return entry.getValue().entrySet().stream().map(yearEntry -> {
                        String year = yearEntry.getKey();
                        double avg = yearEntry.getValue().stream().mapToDouble(d -> d).average().orElse(0.0);
                        return new SalaryMatrixDto(ind, year, Math.round(avg * 10) / 10.0);
                    });
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. 散点图
     */
    public List<ScatterPlotDto> getScatterPlot(JobSearchRequest request) {
        List<LagouData> allJobs = getFilteredData(request);

        Map<String, List<Double>> sizeGroup = new HashMap<>();
        for (LagouData job : allJobs) {
            String size = job.getCompanySize();
            Double salary = SalaryUtil.parseSalary(job.getSalary());
            if (size != null && salary > 0) {
                sizeGroup.computeIfAbsent(size, k -> new ArrayList<>()).add(salary);
            }
        }

        String industryLabel = (request.getIndustry() != null && !request.getIndustry().isEmpty())
                ? request.getIndustry()
                : "全行业";

        return sizeGroup.entrySet().stream()
                .map(entry -> {
                    double avgSalary = entry.getValue().stream().mapToDouble(d -> d).average().orElse(0.0);
                    int count = entry.getValue().size();
                    return new ScatterPlotDto(entry.getKey(), Math.round(avgSalary * 10) / 10.0, count, industryLabel);
                })
                .collect(Collectors.toList());
    }

    /**
     * 3. 词云
     */
    public List<WordCloudDto> getWordCloud(JobSearchRequest request) {
        List<LagouData> allJobs = getFilteredData(request);
        Map<String, Integer> counts = new HashMap<>();

        for (LagouData job : allJobs) {
            String detail = job.getPositionDetail() == null ? "" : job.getPositionDetail();
            List<String> keywords = TextAnalysisUtil.extractKeywords(detail);
            for (String k : keywords) {
                if (k.length() > 1 && !STOP_WORDS.contains(k)) {
                    counts.put(k, counts.getOrDefault(k, 0) + 1);
                }
            }
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(50)
                .map(e -> new WordCloudDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 4. 技能关系网络图 (已修复：防止 ECharts 崩溃)
     */
    public NetworkGraphDto getNetworkGraph(JobSearchRequest request) {
        List<LagouData> jobs = getFilteredData(request);
        Map<String, Integer> nodeWeights = new HashMap<>();
        Map<String, Map<String, Integer>> linksMap = new HashMap<>();

        List<LagouData> sampleJobs = jobs.stream().limit(500).collect(Collectors.toList());

        for (LagouData job : sampleJobs) {
            String detail = job.getPositionDetail() == null ? "" : job.getPositionDetail();
            List<String> skills = TextAnalysisUtil.extractKeywords(detail);

            skills = skills.stream()
                    .distinct()
                    .filter(s -> s.length() > 1)
                    .filter(s -> !STOP_WORDS.contains(s))
                    .collect(Collectors.toList());

            for (String skill : skills) {
                nodeWeights.put(skill, nodeWeights.getOrDefault(skill, 0) + 1);
            }

            for (int i = 0; i < skills.size(); i++) {
                for (int j = i + 1; j < skills.size(); j++) {
                    String s1 = skills.get(i);
                    String s2 = skills.get(j);
                    if (s1.compareTo(s2) > 0) { String temp = s1; s1 = s2; s2 = temp; }

                    linksMap.computeIfAbsent(s1, k -> new HashMap<>())
                            .put(s2, linksMap.get(s1).getOrDefault(s2, 0) + 1);
                }
            }
        }

        List<NetworkGraphDto.Node> nodes = new ArrayList<>();
        // 记录有效的节点名称，用于后续校验 Link
        Set<String> validNodeNames = new HashSet<>();

        for (Map.Entry<String, Integer> entry : nodeWeights.entrySet()) {
            if (entry.getValue() > 2) {
                nodes.add(new NetworkGraphDto.Node(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue(),
                        Math.abs(entry.getKey().hashCode()) % 5,
                        entry.getValue() * 2
                ));
                validNodeNames.add(entry.getKey());
            }
        }

        List<NetworkGraphDto.Link> links = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> srcEntry : linksMap.entrySet()) {
            String source = srcEntry.getKey();
            // 修复：如果源节点已被过滤，则跳过边
            if (!validNodeNames.contains(source)) continue;

            for (Map.Entry<String, Integer> targetEntry : srcEntry.getValue().entrySet()) {
                String target = targetEntry.getKey();
                // 修复：如果目标节点已被过滤，则跳过边
                if (!validNodeNames.contains(target)) continue;

                if (targetEntry.getValue() > 3) {
                    links.add(new NetworkGraphDto.Link(source, target, targetEntry.getValue()));
                }
            }
        }

        List<NetworkGraphDto.Category> categories = Arrays.asList(
                new NetworkGraphDto.Category("后端"), new NetworkGraphDto.Category("前端"),
                new NetworkGraphDto.Category("AI"), new NetworkGraphDto.Category("运维"),
                new NetworkGraphDto.Category("其他")
        );

        return new NetworkGraphDto(nodes, links, categories);
    }

    private String getMainIndustry(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "其他";
        return raw.split("[,，、\\s]")[0].trim();
    }
}