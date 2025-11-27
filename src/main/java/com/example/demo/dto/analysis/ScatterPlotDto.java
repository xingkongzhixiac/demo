package com.example.demo.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;

// 2. 对应 ScatterItem
@Data
@AllArgsConstructor
public class ScatterPlotDto {
    private String companySize;
    private Double avgSalary;
    private Integer count;
    private String industry;
}