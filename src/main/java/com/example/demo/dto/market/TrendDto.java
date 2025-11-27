package com.example.demo.dto.market;

import lombok.AllArgsConstructor;
import lombok.Data;

// 3. 对应 LineItem: { year: string, value: number }
@Data
@AllArgsConstructor
public class TrendDto {
    private String year;
    private Number value;
}
