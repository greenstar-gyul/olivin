package com.olivin.app.dashboard.web;

import com.olivin.app.dashboard.service.SupplierDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard/supplier")
public class SupplierDashboardController {

    private final SupplierDashboardService supplierDashboardService;

    /**
     * 공급업체 목록 조회 (시스템 관리자용)
     * GET /api/dashboard/supplier/suppliers
     */
    @GetMapping("/suppliers")
    public List<Map<String, Object>> getAvailableSuppliers() {
        return supplierDashboardService.getAvailableSuppliers();
    }

    /**
     * 공급업체 정보 조회
     * GET /api/dashboard/supplier/info
     */
    @GetMapping("/info")
    public Map<String, Object> getSupplierInfo(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getSupplierInfo(supplierId);
    }

    /**
     * 공급업체 대시보드 KPI 데이터 조회
     * GET /api/dashboard/supplier/kpi
     */
    @GetMapping("/kpi")
    public Map<String, Object> getKpiData(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getKpiData(supplierId);
    }

    /**
     * 진행 중인 발주 현황 조회
     * GET /api/dashboard/supplier/active-orders
     */
    @GetMapping("/active-orders")
    public List<Map<String, Object>> getActiveOrders(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getActiveOrders(supplierId);
    }

    /**
     * 상위 공급 상품 조회 (TOP 10)
     * GET /api/dashboard/supplier/top-products
     */
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getTopProducts(supplierId);
    }

    /**
     * 월간 수주 트렌드 조회 (최근 6개월)
     * GET /api/dashboard/supplier/order-trend
     */
    @GetMapping("/order-trend")
    public List<Map<String, Object>> getOrderTrend(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getOrderTrend(supplierId);
    }

    /**
     * 카테고리별 공급 비중 조회 (당월)
     * GET /api/dashboard/supplier/category-supply
     */
    @GetMapping("/category-supply")
    public List<Map<String, Object>> getCategorySupply(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getCategorySupply(supplierId);
    }

    /**
     * 품질/배송 관련 알림 조회
     * GET /api/dashboard/supplier/alerts
     */
    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts(@RequestParam(required = false) String supplierId) {
        return supplierDashboardService.getAlerts(supplierId);
    }

    /**
     * 임시 테스트용 - 모든 주문 상태 확인
     */
    @GetMapping("/debug-orders")
    public List<Map<String, Object>> getDebugOrders(@RequestParam String supplierId) {
        return supplierDashboardService.getAllOrderStatus(supplierId);
    }

    /**
     * 디버깅용 - 공급업체별 발주 데이터 상세 정보
     */
    @GetMapping("/debug-info")
    public List<Map<String, Object>> getDebugInfo(@RequestParam String supplierId) {
        return supplierDashboardService.getSupplierOrderDebugInfo(supplierId);
    }
}