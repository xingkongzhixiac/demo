package com.example.demo.dto.market;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HeatMapDto {
    private String name;
    private List<Double> value; // 存放 [经度, 纬度, 数值]
}
