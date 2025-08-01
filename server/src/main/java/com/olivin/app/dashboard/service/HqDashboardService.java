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

    /**
     * 본사 대시보드 KPI 데이터 조회
     */
    public Map<String, Object> getKpiData() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 월간 총 매출액 계산
        BigDecimal monthlyRevenue = hqDashboardMapper.getMonthlyRevenue();
        BigDecimal previousMonthRevenue = hqDashboardMapper.getPreviousMonthRevenue();
        BigDecimal revenueGrowth = calculateGrowthRate(monthlyRevenue, previousMonthRevenue);
        
        result.put("totalSales", formatCurrency(monthlyRevenue));
        result.put("salesGrowth", formatPercentage(revenueGrowth));
        
        // 2. 전체 재고 회전율 계산
        BigDecimal inventoryTurnover = calculateInventoryTurnover();
        BigDecimal previousTurnover = calculatePreviousInventoryTurnover();
        BigDecimal turnoverChange = inventoryTurnover.subtract(previousTurnover);
        
        result.put("inventoryTurnover", inventoryTurnover.setScale(1, RoundingMode.HALF_UP) + "회");
        result.put("turnoverChange", (turnoverChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                   turnoverChange.setScale(1, RoundingMode.HALF_UP) + "회");
        
        // 3. 공급업체 납기준수율 계산
        BigDecimal deliveryRate = hqDashboardMapper.getSupplierDeliveryRate();
        BigDecimal previousDeliveryRate = hqDashboardMapper.getPreviousSupplierDeliveryRate();
        BigDecimal deliveryRateChange = deliveryRate.subtract(previousDeliveryRate);
        
        result.put("deliveryRate", deliveryRate.setScale(1, RoundingMode.HALF_UP) + "%");
        result.put("deliveryRateChange", (deliveryRateChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                       deliveryRateChange.setScale(1, RoundingMode.HALF_UP) + "%");
        
        // 4. 품절률 계산
        BigDecimal stockoutRate = hqDashboardMapper.getStockoutRate();
        BigDecimal previousStockoutRate = hqDashboardMapper.getPreviousStockoutRate();
        BigDecimal stockoutRateChange = stockoutRate.subtract(previousStockoutRate);
        
        result.put("stockoutRate", stockoutRate.setScale(1, RoundingMode.HALF_UP) + "%");
        result.put("stockoutRateChange", (stockoutRateChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                       stockoutRateChange.setScale(1, RoundingMode.HALF_UP) + "%");
        
        return result;
    }

    /**
     * 카테고리별 매출 트렌드 데이터 조회
     */
    public List<Map<String, Object>> getSalesTrend() {
        return hqDashboardMapper.getSalesTrendByCategory();
    }

    /**
     * 카테고리별 매출 구성 데이터 조회
     */
    public List<Map<String, Object>> getCategorySales() {
        List<Map<String, Object>> categorySales = hqDashboardMapper.getCategorySales();
        BigDecimal totalSales = hqDashboardMapper.getMonthlyRevenue();
        
        // 각 카테고리별 비율 계산
        categorySales.forEach(category -> {
            BigDecimal sales = (BigDecimal) category.get("sales");
            BigDecimal percentage = sales.divide(totalSales, 4, RoundingMode.HALF_UP)
                                      .multiply(new BigDecimal("100"));
            category.put("percentage", percentage.setScale(1, RoundingMode.HALF_UP));
        });
        
        return categorySales;
    }

    /**
     * 상위 공급업체 성과 데이터 조회
     */
    public List<Map<String, Object>> getTopSuppliers() {
        return hqDashboardMapper.getTopSuppliers();
    }

    /**
     * 재고 현황 데이터 조회
     */
    public Map<String, Object> getInventoryStatus() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("totalItems", hqDashboardMapper.getTotalInventoryItems());
        result.put("lowStockItems", hqDashboardMapper.getLowStockItems());
        result.put("stockoutItems", hqDashboardMapper.getStockoutItems());
        result.put("totalInventoryValue", formatCurrency(hqDashboardMapper.getTotalInventoryValue()));
        
        // 카테고리별 재고 분포
        result.put("categoryDistribution", hqDashboardMapper.getInventoryByCategory());
        
        return result;
    }

    /**
     * 물류센터 효율성 데이터 조회
     */
    public Map<String, Object> getLogisticsEfficiency() {
        Map<String, Object> result = new HashMap<>();
        
        // 입고 처리 시간 평균
        BigDecimal avgProcessingTime = hqDashboardMapper.getAverageProcessingTime();
        result.put("avgProcessingTime", avgProcessingTime.setScale(1, RoundingMode.HALF_UP) + "시간");
        
        // 당일 입고 처리율
        BigDecimal sameDayProcessingRate = hqDashboardMapper.getSameDayProcessingRate();
        result.put("sameDayProcessingRate", sameDayProcessingRate.setScale(1, RoundingMode.HALF_UP) + "%");
        
        // 창고 가동률
        BigDecimal warehouseUtilization = hqDashboardMapper.getWarehouseUtilization();
        result.put("warehouseUtilization", warehouseUtilization.setScale(1, RoundingMode.HALF_UP) + "%");
        
        // 일일 처리량 트렌드
        result.put("dailyThroughput", hqDashboardMapper.getDailyThroughputTrend());
        
        // 작업 효율성 지표
        result.put("efficiencyMetrics", hqDashboardMapper.getEfficiencyMetrics());
        
        return result;
    }

    /**
     * 긴급 알림 데이터 조회
     */
    public List<Map<String, Object>> getAlerts() {
        List<Map<String, Object>> alerts = hqDashboardMapper.getCriticalAlerts();
        
        // 알림 우선순위 설정
        alerts.forEach(alert -> {
            String type = (String) alert.get("alert_type");
            switch (type) {
                case "STOCKOUT":
                    alert.put("priority", "HIGH");
                    alert.put("color", "#FF4444");
                    break;
                case "LOW_STOCK":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FF8800");
                    break;
                case "DELAYED_DELIVERY":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FF8800");
                    break;
                default:
                    alert.put("priority", "LOW");
                    alert.put("color", "#888888");
            }
        });
        
        return alerts;
    }

    /**
     * 재고 회전율 계산
     */
    private BigDecimal calculateInventoryTurnover() {
        BigDecimal cogs = hqDashboardMapper.getCostOfGoodsSold(); // 매출원가
        BigDecimal avgInventory = hqDashboardMapper.getAverageInventoryValue(); // 평균 재고가치
        
        if (avgInventory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return cogs.divide(avgInventory, 2, RoundingMode.HALF_UP);
    }

    /**
     * 이전 달 재고 회전율 계산
     */
    private BigDecimal calculatePreviousInventoryTurnover() {
        BigDecimal previousCogs = hqDashboardMapper.getPreviousCostOfGoodsSold();
        BigDecimal previousAvgInventory = hqDashboardMapper.getPreviousAverageInventoryValue();
        
        if (previousAvgInventory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return previousCogs.divide(previousAvgInventory, 2, RoundingMode.HALF_UP);
    }

    /**
     * 성장률 계산
     */
    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return current.subtract(previous)
                     .divide(previous, 4, RoundingMode.HALF_UP)
                     .multiply(new BigDecimal("100"));
    }

    /**
     * 통화 포맷팅 (억원 단위)
     */
    private String formatCurrency(BigDecimal amount) {
        BigDecimal inEok = amount.divide(new BigDecimal("100000000"), 1, RoundingMode.HALF_UP);
        return inEok + "억원";
    }

    /**
     * 퍼센티지 포맷팅
     */
    private String formatPercentage(BigDecimal percentage) {
        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percentage.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}