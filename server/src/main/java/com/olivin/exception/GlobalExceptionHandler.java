// GlobalExceptionHandler.java
package com.olivin.exception;

import com.olivin.app.auth.service.ApiResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 유효성 검사 오류 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseVO<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("유효성 검사 오류: {}", errors);
        return ResponseEntity.badRequest()
            .body(ApiResponseVO.error("입력값 검증에 실패했습니다."));
    }
    
    /**
     * 바인딩 오류 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleBindException(BindException ex) {
        String errorMessage = ex.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse("입력값이 올바르지 않습니다.");
        
        log.warn("바인딩 오류: {}", errorMessage);
        return ResponseEntity.badRequest()
            .body(ApiResponseVO.error(errorMessage));
    }
    
    /**
     * 인증 오류 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleAuthenticationException(
            AuthenticationException ex) {
        
        log.warn("인증 오류: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseVO.error("인증에 실패했습니다."));
    }
    
    /**
     * 잘못된 인증 정보 오류 처리
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleBadCredentialsException(
            BadCredentialsException ex) {
        
        log.warn("잘못된 인증 정보: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseVO.error("사원번호 또는 비밀번호가 올바르지 않습니다."));
    }
    
    /**
     * 사용자를 찾을 수 없음 오류 처리
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex) {
        
        log.warn("사용자를 찾을 수 없음: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseVO.error("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 접근 권한 오류 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        log.warn("접근 권한 오류: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseVO.error("접근 권한이 없습니다."));
    }
    
    /**
     * 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseVO<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("런타임 예외 발생: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
            .body(ApiResponseVO.error(ex.getMessage()));
    }
    
    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseVO<Object>> handleException(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
            .body(ApiResponseVO.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}