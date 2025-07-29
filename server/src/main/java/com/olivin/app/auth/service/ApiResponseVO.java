// ApiResponseVO.java
package com.olivin.app.auth.service;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseVO<T> {
    
    private boolean success;    // 성공/실패 여부
    private String message;     // 응답 메시지
    private T data;            // 실제 데이터
    
    // 성공 응답 생성 메서드
    public static <T> ApiResponseVO<T> success(T data) {
        return new ApiResponseVO<>(true, "성공", data);
    }
    
    public static <T> ApiResponseVO<T> success(String message, T data) {
        return new ApiResponseVO<>(true, message, data);
    }
    
    // 실패 응답 생성 메서드
    public static <T> ApiResponseVO<T> error(String message) {
        return new ApiResponseVO<>(false, message, null);
    }
}