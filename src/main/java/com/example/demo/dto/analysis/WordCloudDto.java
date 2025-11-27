package com.example.demo.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;

// 3. 对应 WordItem
@Data
@AllArgsConstructor
public class WordCloudDto {
    private String name;
    private Integer value;
}
