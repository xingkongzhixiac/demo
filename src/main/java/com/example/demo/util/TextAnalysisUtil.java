package com.example.demo.util;

import java.util.*;
import java.util.stream.Collectors;

public class TextAnalysisUtil {

    // 原始关键词库 (保持期望的展示大小写)
    private static final List<String> RAW_KEYWORDS = Arrays.asList(
            "Java", "Python", "C++", "C", "C#", ".NET", "Go", "Golang", "Spring", "SpringBoot", "SpringCloud",
            "MyBatis", "MySQL", "Oracle", "SQLServer", "Redis", "MongoDB", "Elasticsearch", "Kafka",
            "RabbitMQ", "Dubbo", "Netty", "Docker", "Kubernetes", "K8s", "Linux", "Unix",
            "Vue", "React", "Angular", "Node.js", "Node", "TypeScript", "JavaScript", "HTML", "CSS",
            "Hadoop", "Spark", "Flink", "Hive", "Kylin",
            "AI", "AIGC", "NLP", "LLM", "Transformer", "Pytorch", "TensorFlow", "Scikit-learn",
            "Git", "SVN", "Jenkins"
    );

    // 映射表：小写 -> 原始格式 (用于快速查找和还原)
    private static final Map<String, String> KEYWORD_MAP = new HashMap<>();

    static {
        for (String kw : RAW_KEYWORDS) {
            // 存入 map，key 为小写，value 为原始写法
            // 注意：如果有 "Go" 和 "go"，后者会覆盖前者，但这里我们列表是去重的
            KEYWORD_MAP.put(kw.toLowerCase(), kw);
        }
    }

    /**
     * 提取文本中的技术关键词
     * 修复逻辑：解决正则 \b 无法匹配 "C++", "C#" 的问题
     */
    public static List<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        List<String> found = new ArrayList<>();

        // 1. 预处理：转小写
        String lowerText = text.toLowerCase();

        // 2. 将非关键词字符替换为空格
        // 我们允许的关键词字符包括：字母、数字、+, #, ., -
        // 这样 "c++", "c#", "node.js" 就会保持完整，而 "java,python" 会变成 "java python"
        String cleanText = lowerText.replaceAll("[^a-z0-9+#.\\-]", " ");

        // 3. 按空格拆分
        String[] tokens = cleanText.split("\\s+");

        // 4. 匹配
        for (String token : tokens) {
            // 处理可能的边缘标点，比如 "node." -> "node" (简单起见，精确匹配 Map 即可)
            if (KEYWORD_MAP.containsKey(token)) {
                found.add(KEYWORD_MAP.get(token)); // 添加原始大小写的词
            }
            // 针对 C/C++ 的特殊处理，防止被错误分割
            // 比如 token 是 "c++" -> 匹配成功
        }

        return found.stream().distinct().collect(Collectors.toList());
    }
}