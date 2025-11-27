package com.example.demo.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;

// 1. 对应 MatrixItem
@Data
@AllArgsConstructor
public class SalaryMatrixDto {
    private String industry;
    private String workYear;
    private Double avgSalary;
}
