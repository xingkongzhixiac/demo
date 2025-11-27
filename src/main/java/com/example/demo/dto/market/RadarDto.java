package com.example.demo.dto.market;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 4. 对应 RadarItem: { name: string, value: number[] }
@Data
@AllArgsConstructor
public class RadarDto {
    private String name;
    private List<Number> value; // 多维数据
}
