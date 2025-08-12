// AuthController.java
package com.olivin.app.auth.web;

import com.olivin.app.auth.service.ApiResponseVO;
import com.olivin.app.auth.service.LoginRequestVO;
import com.olivin.app.auth.service.LoginResponseVO;
import com.olivin.app.auth.service.AuthService;
import com.olivin.app.auth.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;
    private final MenuService menuService;
    
    /**
     * 로그인
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponseVO<LoginResponseVO>> login(
            @Valid @RequestBody LoginRequestVO request,
            BindingResult bindingResult
    ) {
        log.info("로그인 요청: {}", request.getEmployeeId());
        
        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
            
            log.warn("로그인 유효성 검사 실패: {} - {}", request.getEmployeeId(), errorMessage);
            return ResponseEntity.badRequest()
                .body(ApiResponseVO.error(errorMessage));
        }
        
        try {
            LoginResponseVO response = authService.login(request);
            log.info("로그인 성공: {}", request.getEmployeeId());
            return ResponseEntity.ok(ApiResponseVO.success("로그인이 완료되었습니다.", response));
            
        } catch (RuntimeException e) {
            log.error("로그인 실패: {} - {}", request.getEmployeeId(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponseVO.error(e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 처리 중 서버 오류: {} - {}", request.getEmployeeId(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponseVO.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<ApiResponseVO<LoginResponseVO>> getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("=== /standard/** 접근 ===");
        log.info("현재 사용자: {}", auth.getName());
        log.info("현재 사용자 권한: {}", auth.getAuthorities());
        log.info("인증 상태: {}", auth.isAuthenticated());

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(ApiResponseVO.error("인증되지 않은 사용자입니다."));
            }
            
            String employeeId = authentication.getName();
            log.debug("현재 사용자 정보 조회: {}", employeeId);
            
            LoginResponseVO response = authService.getUserInfo(employeeId);
            
            // 메뉴 정보 추가
            try {
                List<Map<String, Object>> userMenus = menuService.getUserMenus(employeeId);
                response.setMenus(userMenus);
            } catch (Exception e) {
                log.warn("메뉴 정보 조회 실패, 기본 메뉴 사용: {}", e.getMessage());
                // 메뉴 조회 실패 시 역할별 기본 메뉴 제공
                List<Map<String, Object>> defaultMenus = menuService.getDefaultMenusByRole(
                    response.getRole() != null ? response.getRole().getRoleName() : "USER"
                );
                response.setMenus(defaultMenus);
            }
            
            return ResponseEntity.ok(ApiResponseVO.success(response));
            
        } catch (RuntimeException e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponseVO.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponseVO.error("서버 오류가 발생했습니다."));
        }
    }
    
    /**
     * 로그아웃 (클라이언트에서 토큰 삭제)
     */
    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponseVO<Void>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                log.info("로그아웃: {}", authentication.getName());
            }
            
            // JWT는 stateless이므로 서버에서는 특별한 처리 없음
            // 클라이언트에서 토큰을 삭제하도록 안내
            return ResponseEntity.ok(ApiResponseVO.success("로그아웃이 완료되었습니다.", null));
            
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponseVO.error("로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 토큰 유효성 검사
     */
    @PostMapping("/api/auth/validate")
    public ResponseEntity<ApiResponseVO<Boolean>> validateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isValid = authentication != null && authentication.isAuthenticated();
            
            if (isValid) {
                log.debug("토큰 유효성 검사 성공: {}", authentication.getName());
            } else {
                log.debug("토큰 유효성 검사 실패");
            }
            
            return ResponseEntity.ok(ApiResponseVO.success("토큰 검증 완료", isValid));
            
        } catch (Exception e) {
            log.error("토큰 검증 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponseVO.success("토큰 검증 완료", false));
        }
    }
}