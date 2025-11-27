package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.model.LagouData;
import com.example.demo.repository.LagouDataRepository;
import com.example.demo.service.DynamicQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lagou-data")
@RequiredArgsConstructor
public class LagouDataController {

    private final LagouDataRepository lagouDataRepository;
    private final DynamicQueryService dynamicQueryService;

    @GetMapping
    public ApiResponse<Page<LagouData>> getData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String key
    ) {
        // 1. 构建分页对象
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 2. 调用通用查询服务
        // 关键修复：必须传入 lagouDataRepository
        Page<LagouData> result = dynamicQueryService.findWithFiltersAndProjections(
                lagouDataRepository,
                pageable,
                filter,
                key
        );

        return ApiResponse.success(result);
    }
}