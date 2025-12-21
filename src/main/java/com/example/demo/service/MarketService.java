package com.example.demo.service;

import com.example.demo.dto.JobSearchRequest;
import com.example.demo.dto.market.*;
import com.example.demo.model.LagouData;
import com.example.demo.repository.LagouDataRepository;
import com.example.demo.util.GeoUtil;
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
public class MarketService {

    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);
    private static final int MAX_ANALYSIS_ROWS = 5000;

    private final LagouDataRepository lagouDataRepository;
    private final DynamicQueryService dynamicQueryService;

    private List<LagouData> getFilteredData(JobSearchRequest request) {
        Specification<LagouData> spec = dynamicQueryService.buildSpecification(request);
        Pageable limit = PageRequest.of(0, MAX_ANALYSIS_ROWS);
        List<LagouData> rawData = lagouDataRepository.findAll(spec, limit).getContent();

        if (rawData.size() == MAX_ANALYSIS_ROWS) {
            logger.debug("Market analysis data limited to {} rows.", MAX_ANALYSIS_ROWS);
        }

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

    public List<HeatMapDto> getHeatMapData(JobSearchRequest request) {
        List<LagouData> jobs = getFilteredData(request);
        Map<String, Long> cityCounts = jobs.stream()
                .filter(job -> job.getCity() != null && !job.getCity().trim().isEmpty())
                .collect(Collectors.groupingBy(LagouData::getCity, Collectors.counting()));

        return cityCounts.entrySet().stream()
                .map(entry -> {
                    String city = entry.getKey();
                    List<Double> coords = GeoUtil.getCoordinate(city);
                    if (coords == null || coords.size() < 2) return null;
                    List<Double> value = Arrays.asList(coords.get(0), coords.get(1), entry.getValue().doubleValue());
                    return new HeatMapDto(city, value);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NameValueDto> getTopTechStack(JobSearchRequest request) {
        List<LagouData> jobs = getFilteredData(request);
        Map<String, Integer> keywordCounts = new HashMap<>();

        for (LagouData job : jobs) {
            String industry = job.getIndustryField() == null ? "" : job.getIndustryField();
            String detail = job.getPositionDetail() == null ? "" : job.getPositionDetail();
            String text = industry + " " + detail;

            List<String> keywords = TextAnalysisUtil.extractKeywords(text);
            for (String kw : keywords) {
                if (kw.length() > 1) {
                    keywordCounts.put(kw, keywordCounts.getOrDefault(kw, 0) + 1);
                }
            }
        }

        return keywordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(entry -> new NameValueDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<NameValueDto> getFinanceDistribution(JobSearchRequest request) {
        List<LagouData> jobs = getFilteredData(request);
        Map<String, Long> counts = jobs.stream()
                .filter(job -> job.getFinanceStage() != null && !job.getFinanceStage().isEmpty())
                .collect(Collectors.groupingBy(LagouData::getFinanceStage, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> new NameValueDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<TrendDto> getSalaryTrend(JobSearchRequest request) {
        List<LagouData> jobs = getFilteredData(request);
        Map<String, List<Double>> monthSalaries = new HashMap<>();

        for (LagouData job : jobs) {
            String time = job.getCreateTime();
            String salaryStr = job.getSalary();
            if (time != null && time.length() >= 7 && time.contains("-")) {
                try {
                    String month = time.substring(0, 7);
                    Double salary = SalaryUtil.parseSalary(salaryStr);
                    if (salary > 0) {
                        monthSalaries.computeIfAbsent(month, k -> new ArrayList<>()).add(salary);
                    }
                } catch (Exception ignored) {}
            }
        }

        return monthSalaries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    double avg = entry.getValue().stream().mapToDouble(d -> d).average().orElse(0.0);
                    return new TrendDto(entry.getKey(), Math.round(avg * 10.0) / 10.0);
                })
                .collect(Collectors.toList());
    }

//    public List<RadarDto> getAbilityRadar(JobSearchRequest request) {
//        List<RadarDto> list = new ArrayList<>();
//        String industry = request.getIndustry() == null ? "" : request.getIndustry();
//
//        // 修复：Arrays.asList(int...) 生成 List<Integer>，无法赋值给 List<Number>
//        // 需要显式转换为 Number
//        if (industry.contains("AI") || industry.contains("算法")) {
//            list.add(new RadarDto("算法工程师", Arrays.asList(95, 90, 80, 95, 60, 92)));
//            list.add(new RadarDto("行业平均", Arrays.asList(70, 75, 65, 70, 50, 70)));
//        } else if (industry.contains("产品")) {
//            list.add(new RadarDto("高级产品经理", Arrays.asList(60, 85, 95, 70, 95, 90)));
//            list.add(new RadarDto("行业平均", Arrays.asList(50, 65, 70, 60, 75, 70)));
//        } else {
//            list.add(new RadarDto("资深后端", Arrays.asList(90, 85, 95, 80, 70, 88)));
//            list.add(new RadarDto("全栈工程师", Arrays.asList(85, 90, 80, 85, 60, 85)));
//        }
//        return list;
//    }
    public List<RadarDto> getAbilityRadar(JobSearchRequest request) {
        // 1. 获取经过筛选（城市、薪资、年限等）的真实数据
        List<LagouData> jobs = getFilteredData(request);

        // 如果没有数据，返回空或默认值
        if (jobs.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 基于真实数据计算核心指标

        // A. 计算平均薪资 (作为“算法”和“工程”能力的基准)
        // 假设 50k 是满分 100 分
        double avgSalary = jobs.stream()
                .mapToDouble(j -> SalaryUtil.parseSalary(j.getSalary()))
                .average().orElse(0.0);
        int salaryScore = (int) Math.min(95, (avgSalary / 40.0) * 100);
        // 兜底：如果薪资太低，至少给个基础分
        salaryScore = Math.max(50, salaryScore);

        // B. 计算高学历占比 (作为“学历”维度的分数)
        long highEduCount = jobs.stream()
                .filter(j -> j.getEducation() != null && (j.getEducation().contains("硕") || j.getEducation().contains("博")))
                .count();
        int eduScore = (int) Math.min(98, (highEduCount * 1.0 / jobs.size()) * 500 + 60);

        // C. 计算资深工作年限占比 (作为“年限”和“管理”维度的分数)
        long seniorCount = jobs.stream()
                .filter(j -> j.getWorkYear() != null && (j.getWorkYear().contains("5-10") || j.getWorkYear().contains("10年")))
                .count();
        int expScore = (int) Math.min(95, (seniorCount * 1.0 / jobs.size()) * 300 + 50);

        // 3. 构建雷达图数据
        // 维度顺序对应前端: [算法, 工程, 沟通, 管理, 学历, 年限]

        List<RadarDto> list = new ArrayList<>();

        // 数据组1：当前筛选条件下的平均水平
        List<Number> avgValues = Arrays.asList(
                salaryScore * 0.9,     // 算法 (薪资关联)
                salaryScore,           // 工程 (薪资关联)
                70 + Math.random() * 10, // 沟通 (较难量化，给个随机区间)
                expScore * 0.8,        // 管理 (年限关联)
                eduScore,              // 学历
                expScore               // 年限
        );
        list.add(new RadarDto("市场平均", avgValues));

        // 数据组2：高薪/头部人才 (在平均基础上 x 1.2)
        List<Number> topValues = avgValues.stream()
                .map(n -> Math.min(100, n.doubleValue() * 1.25)) // 提升25%，但不超过100
                .collect(Collectors.toList());

        list.add(new RadarDto("高薪标杆", topValues));

        return list;
    }
    private Double roundOneDecimal(double value) {
        // 比如 85.567 -> 855.67 -> 856 -> 85.6
        return Math.round(value * 100.00) / 100.00;
    }
}