package com.olivin.app.auth.web;

import com.olivin.app.auth.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MenuController {

    private final MenuService menuService;

    /**
     * 사용자별 메뉴 목록 조회
     * GET /api/auth/menus
     */
    @GetMapping("/menus")
    public Map<String, Object> getUserMenus(Authentication authentication) {
        log.info("사용자 메뉴 조회 시작 - userId: {}", authentication.getName());
        
        try {
            String userId = authentication.getName();
            List<Map<String, Object>> menus = menuService.getUserMenus(userId);
            
            return Map.of(
                "success", true,
                "data", menus,
                "message", "메뉴 조회 성공"
            );
        } catch (Exception e) {
            log.error("사용자 메뉴 조회 중 오류 발생", e);
            return Map.of(
                "success", false,
                "data", List.of(),
                "message", "메뉴 조회 실패: " + e.getMessage()
            );
        }
    }
}