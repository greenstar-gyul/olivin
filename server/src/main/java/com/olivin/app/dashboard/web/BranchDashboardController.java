package com.olivin.app.dashboard.web;

import com.olivin.app.dashboard.service.BranchDashboardService;
import com.olivin.app.auth.service.UserService;
import com.olivin.app.auth.service.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard/branch")
public class BranchDashboardController {

    private final BranchDashboardService branchDashboardService;
    private final UserService userService;

    /**
     * 현재 사용자 정보 가져오기
     */
    private UserVO getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByEmployeeId(userDetails.getUsername());
    }

    /**
     * 본사 사용자인지 확인
     */
    private boolean isHeadquarterUser(UserVO user) {
        return "system_admin".equals(user.getRoleName()) || 
               "general_manager".equals(user.getRoleName()) ||
               "COM10001".equals(user.getCompId()); // 본사 compId
    }

    /**
     * 접근 가능한 compId 반환 (권한 체크 포함)
     */
    private String getAccessibleCompId(String requestedCompId, Authentication authentication) {
        UserVO user = getCurrentUser(authentication);
        
        if (isHeadquarterUser(user)) {
            // 본사 사용자면 요청한 compId 사용 (모든 지점 접근 가능)
            return requestedCompId != null ? requestedCompId : user.getCompId();
        } else {
            // 지점 사용자면 자신의 compId만 사용 (요청한 compId 무시)
            return user.getCompId();
        }
    }

    /**
     * 지점 정보 조회
     * @param compId 조회할 지점 ID (선택적, 본사만 사용 가능)
     */
    @GetMapping("/info")
    public Map<String, Object> getBranchInfo(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getBranchInfo(accessibleCompId);
    }

    /**
     * 지점 대시보드 KPI 데이터 조회
     */
    @GetMapping("/kpi")
    public Map<String, Object> getBranchKpiData(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getBranchKpiData(accessibleCompId);
    }

    /**
     * 지점별 주간 매출 트렌드 조회 (최근 7일)
     */
    @GetMapping("/sales-trend")
    public List<Map<String, Object>> getWeeklySalesTrend(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getWeeklySalesTrend(accessibleCompId);
    }

    /**
     * 지점별 카테고리별 매출 구성 조회
     */
    @GetMapping("/category-sales")
    public List<Map<String, Object>> getCategorySales(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getCategorySales(accessibleCompId);
    }

    /**
     * 발주 필요 상품 리스트 조회
     */
    @GetMapping("/low-stock")
    public List<Map<String, Object>> getLowStockItems(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getLowStockItems(accessibleCompId);
    }

    /**
     * 품절 임박 상품 리스트 조회
     */
    @GetMapping("/critical-stock")
    public List<Map<String, Object>> getCriticalStockItems(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getCriticalStockItems(accessibleCompId);
    }

    /**
     * 상위 판매 상품 TOP 5 조회
     */
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopSellingProducts(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getTopSellingProducts(accessibleCompId);
    }

    /**
     * 재고 회전율 조회
     */
    @GetMapping("/inventory-turnover")
    public Map<String, Object> getInventoryTurnover(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getInventoryTurnover(accessibleCompId);
    }

    /**
     * 지점 알림 목록 조회
     */
    @GetMapping("/alerts")
    public List<Map<String, Object>> getBranchAlerts(
            @RequestParam(required = false) String compId,
            Authentication authentication) {
        String accessibleCompId = getAccessibleCompId(compId, authentication);
        return branchDashboardService.getBranchAlerts(accessibleCompId);
    }

    /**
     * 본사 전용 - 모든 지점 목록 조회
     */
    @GetMapping("/branches")
    @PreAuthorize("hasRole('system_admin') or hasRole('general_manager')")
    public List<Map<String, Object>> getAllBranches(Authentication authentication) {
        UserVO user = getCurrentUser(authentication);
        if (!isHeadquarterUser(user)) {
            throw new AccessDeniedException("본사 권한이 필요합니다.");
        }
        return branchDashboardService.getAllBranches();
    }

    /**
     * 본사 전용 - 모든 지점 통합 KPI
     */
    @GetMapping("/all-kpi")
    @PreAuthorize("hasRole('system_admin') or hasRole('general_manager')")
    public Map<String, Object> getAllBranchesKpi(Authentication authentication) {
        UserVO user = getCurrentUser(authentication);
        if (!isHeadquarterUser(user)) {
            throw new AccessDeniedException("본사 권한이 필요합니다.");
        }
        return branchDashboardService.getAllBranchesKpi();
    }
}