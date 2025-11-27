package com.example.demo.service;

import com.example.demo.dto.JobSearchRequest;
import com.example.demo.model.LagouData;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DynamicQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicQueryService.class);

    /**
     * 【新增】通用的动态查询方法，供 Controller 直接调用
     * 必须包含此方法，否则 Controller 会报错
     *
     * @param repository 数据仓库 (必须继承 JpaSpecificationExecutor)
     * @param pageable   分页参数
     * @param filter     过滤字符串 (格式: "fieldName:value")
     * @param key        全局搜索关键字
     */
    public <T> Page<T> findWithFiltersAndProjections(
            JpaSpecificationExecutor<T> repository,
            Pageable pageable,
            String filter,
            String key) {

        Specification<T> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 处理 Filter (精确匹配)
            if (StringUtils.hasText(filter)) {
                String[] conditions = filter.split(",");
                for (String condition : conditions) {
                    String[] parts = condition.split(":", 2);
                    if (parts.length == 2) {
                        String fieldName = parts[0].trim();
                        String value = parts[1].trim();
                        if (StringUtils.hasText(fieldName)) {
                            try {
                                predicates.add(cb.equal(root.get(fieldName), value));
                            } catch (IllegalArgumentException e) {
                                // 忽略不存在的字段
                            }
                        }
                    }
                }
            }

            // 2. 处理 Key (模糊搜索)
            if (StringUtils.hasText(key)) {
                String likePattern = "%" + key + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                // 尝试在常用字段中搜索 (使用 try-catch 忽略不适用的实体字段)
                try { searchPredicates.add(cb.like(root.get("positionName"), likePattern)); } catch (Exception ignored) {}
                try { searchPredicates.add(cb.like(root.get("companyName"), likePattern)); } catch (Exception ignored) {}
                try { searchPredicates.add(cb.like(root.get("companyFullName"), likePattern)); } catch (Exception ignored) {}
                try { searchPredicates.add(cb.like(root.get("city"), likePattern)); } catch (Exception ignored) {}

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }

    /**
     * 【保留】原有的特定业务查询构建器 (供 AnalysisService 等使用)
     */
    public Specification<LagouData> buildSpecification(JobSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getCity()) && !"全国".equals(request.getCity())) {
                predicates.add(cb.like(root.get("city"), "%" + request.getCity() + "%"));
            }
            if (StringUtils.hasText(request.getIndustry())) {
                predicates.add(cb.like(root.get("industryField"), "%" + request.getIndustry() + "%"));
            }
            if (StringUtils.hasText(request.getFinanceStage())) {
                predicates.add(cb.equal(root.get("financeStage"), request.getFinanceStage()));
            }
            if (StringUtils.hasText(request.getWorkYear()) && !"不限".equals(request.getWorkYear())) {
                predicates.add(cb.equal(root.get("workYear"), request.getWorkYear()));
            }
            // 兼容搜索词
            if (StringUtils.hasText(request.getKey())) {
                String likePattern = "%" + request.getKey() + "%";
                Predicate posLike = cb.like(root.get("positionName"), likePattern);
                Predicate comLike = cb.like(root.get("companyFullName"), likePattern);
                predicates.add(cb.or(posLike, comLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}