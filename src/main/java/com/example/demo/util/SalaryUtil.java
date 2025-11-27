package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaryUtil {

    private static final Logger logger = LoggerFactory.getLogger(SalaryUtil.class);

    // 匹配 "1.5k-2.5k", "15000-20000"
    private static final Pattern SALARY_RANGE = Pattern.compile("(\\d+(\\.\\d+)?)\\s*[kK]?\\s*[-~]\\s*(\\d+(\\.\\d+)?)\\s*[kK]?");

    // 匹配 "2.5k", "25000"
    private static final Pattern SALARY_SINGLE = Pattern.compile("(\\d+(\\.\\d+)?)\\s*[kK]?(?=\\b|[^a-zA-Z0-9.])");

    // 阈值：超过 500k 的月薪通常是单位错误（元 vs k）
    private static final double RAW_SALARY_THRESHOLD = 500.0;
    // 最终统计时的最大有效值 (例如 200k，超过这个可能也是脏数据)
    private static final double MAX_VALID_SALARY = 200.0;

    public static Double parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.trim().isEmpty()) return 0.0;

        // 排除干扰数据
        if (salaryStr.contains("天") || salaryStr.contains("面议") || salaryStr.contains("年薪")) {
            return 0.0;
        }

        try {
            // 清洗：去掉 "·16薪" 等后缀，保留数字、k、-、.
            String cleanStr = salaryStr.split("·")[0].replaceAll("[^0-9a-zA-Z\\-~.]", "").toLowerCase().trim();

            double min = 0.0;
            double max = 0.0;

            Matcher rangeMatcher = SALARY_RANGE.matcher(cleanStr);
            if (rangeMatcher.find()) {
                // group(1) 是第一个数字, group(3) 是第二个数字 (注意嵌套分组索引)
                min = Double.parseDouble(rangeMatcher.group(1));
                max = Double.parseDouble(rangeMatcher.group(3));
            } else {
                Matcher singleMatcher = SALARY_SINGLE.matcher(cleanStr);
                if (singleMatcher.find()) {
                    min = max = Double.parseDouble(singleMatcher.group(1));
                }
            }

            if (min > 0 && max > 0) {
                double avg = (min + max) / 2.0;

                // 核心修复：自动归一化 (处理 "20000" 这种单位为元的脏数据)
                if (avg > RAW_SALARY_THRESHOLD) {
                    avg = avg / 1000.0;
                }

                // 二次校验
                if (avg > MAX_VALID_SALARY) {
                    return 0.0; // 过滤离谱数据
                }
                return avg;
            }

        } catch (Exception e) {
            logger.debug("Failed to parse salary: {}", salaryStr);
        }
        return 0.0;
    }
}