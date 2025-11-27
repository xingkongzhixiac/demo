package com.example.demo.dto.intelligence;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 1. 对应 SalaryDistributionItem (箱线图)
@Data
@AllArgsConstructor
public class BoxPlotDto {
    private String industry;
    // 数组严格顺序：[min, Q1, median, Q3, max]
    private List<Double> values;
}
