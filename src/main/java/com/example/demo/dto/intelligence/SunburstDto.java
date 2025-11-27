package com.example.demo.dto.intelligence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor // 生成全参构造函数 (name, value, children, itemStyle)
@NoArgsConstructor
public class SunburstDto {
    private String name;
    private Double value;
    private List<SunburstDto> children;

    // 对应前端的 itemStyle?: { color?: string }
    private ItemStyle itemStyle;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemStyle {
        private String color;
    }

    // --- 辅助方法：为了方便 Service 层递归构建 ---

    // 兼容旧代码的辅助构造函数 (仅 name)
    public SunburstDto(String name) {
        this.name = name;
    }

    // 辅助方法：添加子节点
    public void addChild(SunburstDto child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
}