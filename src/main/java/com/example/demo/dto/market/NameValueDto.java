package com.example.demo.dto.market;

import lombok.AllArgsConstructor;
import lombok.Data;

// 2. 对应 BarItem 和 PieItem: { name: string, value: number }
@Data
@AllArgsConstructor
public class NameValueDto {
    private String name;
    private Number value; // 使用 Number 兼容 Integer/Long/Double
}
