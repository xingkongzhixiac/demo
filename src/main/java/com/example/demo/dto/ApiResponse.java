package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

@Getter
//public class ApiResponse<T> {
//
//    private final int code;
//    private final String message;
//    private final T data;
//
//    private ApiResponse(int code, String message, T data) {
//        this.code = code;
//        this.message = message;
//        this.data = data;
//    }
//
//    public static <T> ApiResponse<T> success(T data) {
//        // 如果数据是Page类型，我们将其转换为包含分页信息的Map
//        if (data instanceof Page) {
//            Page<?> pageData = (Page<?>) data;
//            Map<String, Object> pageMap = new HashMap<>();
//            pageMap.put("content", pageData.getContent());
//            pageMap.put("currentPage", pageData.getNumber() + 1); // 返回给前端页码从1开始
//            pageMap.put("pageSize", pageData.getSize());
//            pageMap.put("totalPages", pageData.getTotalPages());
//            pageMap.put("totalElements", pageData.getTotalElements());
//            return new ApiResponse<>(200, "Success", (T) pageMap);
//        }
//        return new ApiResponse<>(200, "Success", data);
//    }
//
//    public static <T> ApiResponse<T> error(int code, String message) {
//        return new ApiResponse<>(code, message, null);
//    }
//}
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;      // 对应前端 code
    private String msg;    // 对应前端 msg (注意：不是 message)
    private T data;        // 对应前端 data

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}