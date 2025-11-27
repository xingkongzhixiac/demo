package com.example.demo.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 4. 对应 NetworkData (比较复杂，包含节点和边)
@Data
@AllArgsConstructor
public class NetworkGraphDto {
    private List<Node> nodes;
    private List<Link> links;
    private List<Category> categories;

    @Data
    @AllArgsConstructor
    public static class Node {
        private String id;
        private String name;
        private Number value;
        private Integer category;
        private Integer symbolSize;
        // x, y 由后端计算或前端布局算法决定，后端可不传或传 null
    }

    @Data
    @AllArgsConstructor
    public static class Link {
        private String source;
        private String target;
        private Number value;
    }

    @Data
    @AllArgsConstructor
    public static class Category {
        private String name;
    }
}
