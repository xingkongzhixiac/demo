package com.example.demo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地理信息工具类
 * 负责加载 resources/data 下的 JSON 配置文件，提供坐标查询和省市映射功能。
 */
public class GeoUtil {
    private static final Logger logger = LoggerFactory.getLogger(GeoUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 缓存：省份 -> 城市列表 (从 json 加载)
    private static Map<String, List<String>> PROVINCE_CITY_MAP = new ConcurrentHashMap<>();

    // 缓存：城市 -> 坐标 (从 json 加载)
    private static Map<String, List<Double>> CITY_COORDINATES = new ConcurrentHashMap<>();

    // 静态代码块：类加载时读取数据
    static {
        loadData();
    }

    private static void loadData() {
        try {
            // 1. 加载城市坐标
            ClassPathResource coordsResource = new ClassPathResource("data/city_coordinates.json");
            if (coordsResource.exists()) {
                try (InputStream is = coordsResource.getInputStream()) {
                    CITY_COORDINATES = objectMapper.readValue(is, new TypeReference<Map<String, List<Double>>>() {});
                    logger.info("Loaded {} city coordinates.", CITY_COORDINATES.size());
                }
            } else {
                logger.warn("city_coordinates.json not found!");
            }

            // 2. 加载省市映射
            ClassPathResource mapResource = new ClassPathResource("data/province_city_map.json");
            if (mapResource.exists()) {
                try (InputStream is = mapResource.getInputStream()) {
                    PROVINCE_CITY_MAP = objectMapper.readValue(is, new TypeReference<Map<String, List<String>>>() {});
                    logger.info("Loaded {} province mappings.", PROVINCE_CITY_MAP.size());
                }
            } else {
                logger.warn("province_city_map.json not found!");
            }

        } catch (Exception e) {
            logger.error("Failed to load Geo JSON data", e);
        }
    }

    /**
     * 获取城市坐标 [lng, lat]
     */
    public static List<Double> getCoordinate(String city) {
        if (city == null || city.trim().isEmpty()) return null;
        return CITY_COORDINATES.get(city.trim());
    }

    /**
     * 获取映射后的城市列表
     * 输入 "四川" -> 返回 ["成都", "绵阳", ...]
     * 输入 "成都" -> 返回 ["成都"]
     */
    public static List<String> getMappedCities(String input) {
        if (input == null || input.trim().isEmpty()) return Collections.emptyList();
        String key = input.trim();

        // 1. 尝试作为省份查找
        if (PROVINCE_CITY_MAP.containsKey(key)) {
            return PROVINCE_CITY_MAP.get(key);
        }

        // 2. 尝试处理带“省”字的情况 (如前端传了 "四川省")
        String shortName = key.replace("省", "").replace("市", "").replace("自治区", "");
        if (PROVINCE_CITY_MAP.containsKey(shortName)) {
            return PROVINCE_CITY_MAP.get(shortName);
        }

        // 3. 默认返回自己 (视为直辖市或具体城市)
        return Collections.singletonList(key);
    }
}