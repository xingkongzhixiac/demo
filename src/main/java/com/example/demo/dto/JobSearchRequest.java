package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class JobSearchRequest {
    private String city;
    private String industry;
    private String financeStage;
    private String workYear;

    // 对应前端: salaryRange: [number, number]
    private List<Integer> salaryRange;

    // --- 修复点：增加搜索关键字字段，对应 filterStore.searchQuery ---
    private String key;
}