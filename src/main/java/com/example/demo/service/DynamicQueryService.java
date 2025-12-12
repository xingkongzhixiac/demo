package com.example.demo.service;

import com.example.demo.dto.JobSearchRequest;
import com.example.demo.model.LagouData;
import com.example.demo.util.GeoUtil;
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
     * 通用的动态查询方法 (用于后台管理或数据表格)
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

                // 尝试在常用字段中搜索
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
     * 【特定业务查询构建器】(用于可视化分析)
     * 核心逻辑：解决地图点击省份后，查询城市数据的问题
     */
    public Specification<LagouData> buildSpecification(JobSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 处理城市/省份逻辑
            if (StringUtils.hasText(request.getCity()) && !"全国".equals(request.getCity())) {
                String inputCity = request.getCity();

                // 获取映射列表 (如果是省份，会返回该省下的多个城市；如果是城市，返回单个)
                List<String> mappedCities = GeoUtil.getMappedCities(inputCity);

                if (mappedCities != null && !mappedCities.isEmpty()) {
                    if (mappedCities.size() == 1) {
                        // 单个城市，直接 Like
                        predicates.add(cb.like(root.get("city"), "%" + mappedCities.get(0) + "%"));
                    } else {
                        // 省份模式：构建 OR 查询 (city LIKE %成都% OR city LIKE %绵阳% ...)
                        List<Predicate> cityPredicates = new ArrayList<>();
                        for (String city : mappedCities) {
                            cityPredicates.add(cb.like(root.get("city"), "%" + city + "%"));
                        }
                        predicates.add(cb.or(cityPredicates.toArray(new Predicate[0])));
                    }
                } else {
                    // 兜底：如果没有映射且非空，就按原值查
                    predicates.add(cb.like(root.get("city"), "%" + inputCity + "%"));
                }
            }

            // 2. 行业
            if (StringUtils.hasText(request.getIndustry())) {
                predicates.add(cb.like(root.get("industryField"), "%" + request.getIndustry() + "%"));
            }

            // 3. 融资阶段
            if (StringUtils.hasText(request.getFinanceStage())) {
                predicates.add(cb.equal(root.get("financeStage"), request.getFinanceStage()));
            }

            // 4. 工作年限
            if (StringUtils.hasText(request.getWorkYear()) && !"不限".equals(request.getWorkYear())) {
                predicates.add(cb.equal(root.get("workYear"), request.getWorkYear()));
            }

            // 5. 搜索关键字 (对应前端的 filterStore.searchQuery 或 request.key)
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