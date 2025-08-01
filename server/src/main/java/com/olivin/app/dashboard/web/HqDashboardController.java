package com.olivin.app.dashboard.web;

import com.olivin.app.dashboard.service.HqDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard/hq")
public class HqDashboardController {

    private final HqDashboardService hqDashboardService;

    /**
     * 본사 대시보드 KPI 데이터 조회
     * GET /api/dashboard/hq/kpi
     */
    @GetMapping("/kpi")
    public Map<String, Object> getKpiData() {
        return hqDashboardService.getKpiData();
    }

    /**
     * 카테고리별 매출 트렌드 조회 (최근 6개월)
     * GET /api/dashboard/hq/sales-trend
     */
    @GetMapping("/sales-trend")
    public List<Map<String, Object>> getSalesTrend() {
        return hqDashboardService.getSalesTrend();
    }

    /**
     * 카테고리별 매출 구성 조회 (당월)
     * GET /api/dashboard/hq/category-sales
     */
    @GetMapping("/category-sales")
    public List<Map<String, Object>> getCategorySales() {
        return hqDashboardService.getCategorySales();
    }

    /**
     * 상위 공급업체 성과 조회 (TOP 5)
     * GET /api/dashboard/hq/suppliers
     */
    @GetMapping("/suppliers")
    public List<Map<String, Object>> getTopSuppliers() {
        return hqDashboardService.getTopSuppliers();
    }

    /**
     * 재고 현황 조회
     * GET /api/dashboard/hq/inventory
     */
    @GetMapping("/inventory")
    public Map<String, Object> getInventoryStatus() {
        return hqDashboardService.getInventoryStatus();
    }

    /**
     * 물류센터 효율성 조회
     * GET /api/dashboard/hq/logistics
     */
    @GetMapping("/logistics")
    public Map<String, Object> getLogisticsEfficiency() {
        return hqDashboardService.getLogisticsEfficiency();
    }

    /**
     * 긴급 알림 조회
     * GET /api/dashboard/hq/alerts
     */
    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts() {
        return hqDashboardService.getAlerts();
    }
}