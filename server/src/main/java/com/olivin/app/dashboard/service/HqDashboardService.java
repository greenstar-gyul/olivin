package com.olivin.app.dashboard.service;

import com.olivin.app.dashboard.mapper.HqDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HqDashboardService {

    private final HqDashboardMapper hqDashboardMapper;

    public Map<String, Object> getKpiData() {
        
        System.out.println("=== 새로운 KPI 데이터 조회 시작 ===");
        
        Map<String, Object> result = new HashMap<>();
        
        // 1. 월간 총 매출액 & 전월 대비 매출 증감율
        BigDecimal monthlyRevenue = hqDashboardMapper.getMonthlyRevenue();
        BigDecimal previousMonthRevenue = hqDashboardMapper.getPreviousMonthRevenue();
        BigDecimal revenueGrowthRate = calculateGrowthRate(monthlyRevenue, previousMonthRevenue);
        
        result.put("totalSales", formatCurrency(monthlyRevenue));
        result.put("salesGrowth", formatPercentage(revenueGrowthRate));
        
        System.out.println("매출 데이터 - 현재: " + monthlyRevenue + ", 이전: " + previousMonthRevenue);
        
        // 2. 🔥 새로운 KPI: 전월 대비 매출 증감율 (위에서 이미 계산됨)
        result.put("revenueGrowthRate", formatPercentage(revenueGrowthRate));
        result.put("revenueGrowthChange", "매출 증감율"); // 단순 레이블
        
        // 3. 🔥 새로운 KPI: 출고 대기 건수
        Integer pendingOutboundCount = hqDashboardMapper.getPendingOutboundCount();
        Integer previousPendingOutboundCount = hqDashboardMapper.getPreviousPendingOutboundCount();
        Integer outboundCountChange = pendingOutboundCount - previousPendingOutboundCount;
        
        result.put("pendingOutboundCount", pendingOutboundCount + "건");
        result.put("outboundCountChange", (outboundCountChange >= 0 ? "+" : "") + outboundCountChange + "건");
        
        System.out.println("출고 대기 - 현재: " + pendingOutboundCount + ", 이전: " + previousPendingOutboundCount);
        
        // 4. 🔥 새로운 KPI: 대기중인 발주서 수
        Integer pendingPOCount = hqDashboardMapper.getPendingPurchaseOrderCount();
        Integer previousPendingPOCount = hqDashboardMapper.getPreviousPendingPurchaseOrderCount();
        Integer poCountChange = pendingPOCount - previousPendingPOCount;
        
        result.put("pendingPurchaseOrderCount", pendingPOCount + "건");
        result.put("poCountChange", (poCountChange >= 0 ? "+" : "") + poCountChange + "건");
        
        System.out.println("발주서 대기 - 현재: " + pendingPOCount + ", 이전: " + previousPendingPOCount);
        System.out.println("=== 새로운 KPI 데이터 조회 완료 ===");
        
        return result;
    }

    public List<Map<String, Object>> getSalesTrend() {
        return hqDashboardMapper.getSalesTrendByCategory();
    }

    public List<Map<String, Object>> getCategorySales() {
        List<Map<String, Object>> categorySales = hqDashboardMapper.getCategorySales();
        BigDecimal totalSales = hqDashboardMapper.getMonthlyRevenue();
        
        categorySales.forEach(category -> {
            BigDecimal sales = (BigDecimal) category.get("sales");
            
            if (sales != null && totalSales != null && totalSales.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = sales.divide(totalSales, 4, RoundingMode.HALF_UP)
                                          .multiply(new BigDecimal("100"));
                category.put("percentage", percentage.setScale(1, RoundingMode.HALF_UP));
            } else {
                category.put("percentage", BigDecimal.ZERO);
            }
        });
        
        return categorySales;
    }

    public List<Map<String, Object>> getTopSuppliers() {
        return hqDashboardMapper.getTopSuppliers();
    }

    public Map<String, Object> getInventoryStatus() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("totalItems", hqDashboardMapper.getTotalInventoryItems());
        result.put("lowStockItems", hqDashboardMapper.getLowStockItems());
        result.put("stockoutItems", hqDashboardMapper.getStockoutItems());
        result.put("totalInventoryValue", formatCurrency(hqDashboardMapper.getTotalInventoryValue()));
        
        result.put("categoryDistribution", hqDashboardMapper.getInventoryByCategory());
        
        return result;
    }

    public Map<String, Object> getLogisticsEfficiency() {
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal avgProcessingTime = hqDashboardMapper.getAverageProcessingTime();
        result.put("avgProcessingTime", avgProcessingTime.setScale(1, RoundingMode.HALF_UP) + "시간");
        
        result.put("sameDayProcessingRate", "85.0%");
        result.put("warehouseUtilization", "72.5%");
        result.put("dailyThroughput", List.of());
        result.put("efficiencyMetrics", List.of());
        
        return result;
    }

    public List<Map<String, Object>> getAlerts() {
        List<Map<String, Object>> alerts = hqDashboardMapper.getCriticalAlerts();
        
        alerts.forEach(alert -> {
            String type = (String) alert.get("alert_type");
            
            if (type == null) {
                type = "INFO";
                alert.put("alert_type", type);
            }
            
            // 🔥 새로운 알림 타입들 처리
            switch (type) {
                case "PENDING_OUTBOUND":
                    alert.put("priority", "HIGH");
                    alert.put("color", "#4299e1");
                    break;
                case "PENDING_PURCHASE_ORDER":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#ed8936");
                    break;
                case "STOCKOUT_WARNING":
                case "STOCKOUT":
                    alert.put("priority", "HIGH");
                    alert.put("color", "#f56565");
                    break;
                case "LOW_STOCK":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#fbd38d");
                    break;
                case "DELIVERY_DELAY":
                case "DELAYED_DELIVERY":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#ff8800");
                    break;
                case "LOW_TURNOVER":
                    alert.put("priority", "LOW");
                    alert.put("color", "#a0aec0");
                    break;
                default:
                    alert.put("priority", "LOW");
                    alert.put("color", "#888888");
            }
        });
        
        return alerts;
    }

    // 🔥 삭제: 재고 회전율 계산 메서드들
    /*
    private BigDecimal calculateInventoryTurnover() { ... }
    private BigDecimal calculatePreviousInventoryTurnover() { ... }
    */

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        
        return current.subtract(previous)
                     .divide(previous, 4, RoundingMode.HALF_UP)
                     .multiply(new BigDecimal("100"));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0원";
        }
        
        if (amount.compareTo(new BigDecimal("100000000")) >= 0) {
            BigDecimal inEok = amount.divide(new BigDecimal("100000000"), 1, RoundingMode.HALF_UP);
            return inEok + "억원";
        } else if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            BigDecimal inMan = amount.divide(new BigDecimal("10000"), 1, RoundingMode.HALF_UP);
            return inMan + "만원";
        } else {
            return amount.intValue() + "원";
        }
    }

    private String formatPercentage(BigDecimal percentage) {
        if (percentage == null) {
            return "0.0%";
        }
        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percentage.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}