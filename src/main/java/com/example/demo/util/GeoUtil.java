package com.example.demo.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoUtil {
    private static final Map<String, List<Double>> CITY_COORDINATES = new HashMap<>();

    static {
        // 预置常用城市坐标 [经度, 纬度]
        CITY_COORDINATES.put("北京", List.of(116.4074, 39.9042));
        CITY_COORDINATES.put("上海", List.of(121.4737, 31.2304));
        CITY_COORDINATES.put("广州", List.of(113.2644, 23.1291));
        CITY_COORDINATES.put("深圳", List.of(114.0859, 22.547));
        CITY_COORDINATES.put("杭州", List.of(120.1551, 30.2741));
        CITY_COORDINATES.put("成都", List.of(104.0668, 30.5728));
        CITY_COORDINATES.put("武汉", List.of(114.3054, 30.5931));
        CITY_COORDINATES.put("西安", List.of(108.9398, 34.3416));
        CITY_COORDINATES.put("南京", List.of(118.7969, 32.0603));
        CITY_COORDINATES.put("苏州", List.of(120.5853, 31.2989));
        CITY_COORDINATES.put("天津", List.of(117.2009, 39.084158));
        CITY_COORDINATES.put("重庆", List.of(106.5516, 29.5630));
        CITY_COORDINATES.put("长沙", List.of(112.9388, 28.2282));
        CITY_COORDINATES.put("郑州", List.of(113.6253, 34.7466));
        CITY_COORDINATES.put("青岛", List.of(120.3826, 36.0671));
        CITY_COORDINATES.put("合肥", List.of(117.2272, 31.8206));
        CITY_COORDINATES.put("佛山", List.of(113.1214, 23.0215));
        CITY_COORDINATES.put("东莞", List.of(113.7518, 23.0206));
        CITY_COORDINATES.put("沈阳", List.of(123.4315, 41.8057));
        CITY_COORDINATES.put("大连", List.of(121.6147, 38.9140));
        CITY_COORDINATES.put("厦门", List.of(118.0894, 24.4798));

        //华北地区
        CITY_COORDINATES.put("石家庄", List.of(114.5149, 38.0428));
        CITY_COORDINATES.put("太原", List.of(112.5492, 37.8706));
        CITY_COORDINATES.put("呼和浩特", List.of(111.7656, 40.8175));
        CITY_COORDINATES.put("唐山", List.of(118.1754, 39.6351));
        CITY_COORDINATES.put("保定", List.of(115.4825, 38.8676));
        CITY_COORDINATES.put("张家口", List.of(114.8841, 40.8119));

        //东北地区
        CITY_COORDINATES.put("长春", List.of(125.3235, 43.8171));
        CITY_COORDINATES.put("哈尔滨", List.of(126.5301, 45.8038));
        CITY_COORDINATES.put("吉林", List.of(126.5530, 43.8430));
        CITY_COORDINATES.put("鞍山", List.of(122.9942, 41.1087));
        CITY_COORDINATES.put("大庆", List.of(125.0218, 46.5895));

        //华东地区
        CITY_COORDINATES.put("济南", List.of(117.1205, 36.6519));
        CITY_COORDINATES.put("南昌", List.of(115.8582, 28.6829));
        CITY_COORDINATES.put("福州", List.of(119.2965, 26.0745));
        CITY_COORDINATES.put("宁波", List.of(121.5498, 29.8683));
        CITY_COORDINATES.put("温州", List.of(120.6994, 27.9939));
        CITY_COORDINATES.put("无锡", List.of(120.3124, 31.4907));
        CITY_COORDINATES.put("徐州", List.of(117.1848, 34.2619));
        CITY_COORDINATES.put("烟台", List.of(121.4478, 37.4638));
        CITY_COORDINATES.put("泉州", List.of(118.6757, 24.8741));

        //华中地区
        CITY_COORDINATES.put("洛阳", List.of(112.4540, 34.6197));
        CITY_COORDINATES.put("宜昌", List.of(111.2865, 30.6919));
        CITY_COORDINATES.put("襄阳", List.of(112.1222, 32.0090));
        CITY_COORDINATES.put("岳阳", List.of(113.1288, 29.3570));
        CITY_COORDINATES.put("衡阳", List.of(112.6077, 26.9004));

        //华南地区
        CITY_COORDINATES.put("南宁", List.of(108.3669, 22.8162));
        CITY_COORDINATES.put("海口", List.of(110.3312, 20.0319));
        CITY_COORDINATES.put("三亚", List.of(109.5083, 18.2525));
        CITY_COORDINATES.put("珠海", List.of(113.5764, 22.2707));
        CITY_COORDINATES.put("惠州", List.of(114.4152, 23.1124));
        CITY_COORDINATES.put("中山", List.of(113.3928, 22.5176));
        CITY_COORDINATES.put("柳州", List.of(109.4280, 24.3265));
        CITY_COORDINATES.put("桂林", List.of(110.2993, 25.2740));

        //西南地区
        CITY_COORDINATES.put("昆明", List.of(102.7123, 25.0406));
        CITY_COORDINATES.put("贵阳", List.of(106.7073, 26.5982));
        CITY_COORDINATES.put("拉萨", List.of(91.1322, 29.6604));
        CITY_COORDINATES.put("绵阳", List.of(104.6791, 31.4675));
        CITY_COORDINATES.put("宜宾", List.of(104.6308, 28.7602));
        CITY_COORDINATES.put("南充", List.of(106.1107, 30.8378));
        CITY_COORDINATES.put("遵义", List.of(106.9274, 27.7257));
        CITY_COORDINATES.put("曲靖", List.of(103.7962, 25.4900));

        //西北地区
        CITY_COORDINATES.put("兰州", List.of(103.8343, 36.0611));
        CITY_COORDINATES.put("西宁", List.of(101.7789, 36.6232));
        CITY_COORDINATES.put("银川", List.of(106.2782, 38.4664));
        CITY_COORDINATES.put("乌鲁木齐", List.of(87.6177, 43.7928));
        CITY_COORDINATES.put("咸阳", List.of(108.7091, 34.3299));
        CITY_COORDINATES.put("宝鸡", List.of(107.1449, 34.3693));
        CITY_COORDINATES.put("榆林", List.of(109.7340, 38.2854));
        CITY_COORDINATES.put("喀什", List.of(75.9896, 39.4704));
        CITY_COORDINATES.put("伊犁", List.of(81.3179, 43.9219));
    }

    public static List<Double> getCoordinate(String city) {
        if (city == null || city.trim().isEmpty()) return null;
        // 尝试获取，忽略前后空格
        return CITY_COORDINATES.get(city.trim());
    }
}