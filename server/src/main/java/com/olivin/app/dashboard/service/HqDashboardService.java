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

        BigDecimal debugDeliveryRate = hqDashboardMapper.getSupplierDeliveryRate();
        BigDecimal debugStockoutRate = hqDashboardMapper.getStockoutRate();
        BigDecimal debugCogs = hqDashboardMapper.getCostOfGoodsSold();
        BigDecimal debugAvgInventory = hqDashboardMapper.getAverageInventoryValue();
        
        System.out.println("=== KPI 디버깅 ===");
        System.out.println("deliveryRate: " + debugDeliveryRate);
        System.out.println("stockoutRate: " + debugStockoutRate);
        System.out.println("cogs: " + debugCogs);
        System.out.println("avgInventory: " + debugAvgInventory);

        Map<String, Object> result = new HashMap<>();
        
        BigDecimal monthlyRevenue = hqDashboardMapper.getMonthlyRevenue();
        BigDecimal previousMonthRevenue = hqDashboardMapper.getPreviousMonthRevenue();
        BigDecimal revenueGrowth = calculateGrowthRate(monthlyRevenue, previousMonthRevenue);
        
        result.put("totalSales", formatCurrency(monthlyRevenue));
        result.put("salesGrowth", formatPercentage(revenueGrowth));
        
        BigDecimal inventoryTurnover = calculateInventoryTurnover();
        BigDecimal previousTurnover = calculatePreviousInventoryTurnover();
        BigDecimal turnoverChange = inventoryTurnover.subtract(previousTurnover);
        
        result.put("inventoryTurnover", inventoryTurnover.setScale(1, RoundingMode.HALF_UP) + "회");
        result.put("turnoverChange", (turnoverChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                   turnoverChange.setScale(1, RoundingMode.HALF_UP) + "회");
        
        BigDecimal deliveryRate = hqDashboardMapper.getSupplierDeliveryRate();
        BigDecimal previousDeliveryRate = hqDashboardMapper.getPreviousSupplierDeliveryRate();
        BigDecimal deliveryRateChange = deliveryRate.subtract(previousDeliveryRate);
        
        result.put("deliveryRate", deliveryRate.setScale(1, RoundingMode.HALF_UP) + "%");
        result.put("deliveryRateChange", (deliveryRateChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                       deliveryRateChange.setScale(1, RoundingMode.HALF_UP) + "%");
        
        BigDecimal stockoutRate = hqDashboardMapper.getStockoutRate();
        BigDecimal previousStockoutRate = hqDashboardMapper.getPreviousStockoutRate();
        BigDecimal stockoutRateChange = stockoutRate.subtract(previousStockoutRate);
        
        result.put("stockoutRate", stockoutRate.setScale(1, RoundingMode.HALF_UP) + "%");
        result.put("stockoutRateChange", (stockoutRateChange.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + 
                                       stockoutRateChange.setScale(1, RoundingMode.HALF_UP) + "%");
        
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
            
            switch (type) {
                case "STOCKOUT_WARNING":
                case "STOCKOUT":
                    alert.put("priority", "HIGH");
                    alert.put("color", "#FF4444");
                    break;
                case "LOW_STOCK":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FF8800");
                    break;
                case "DELIVERY_DELAY":
                case "DELAYED_DELIVERY":
                    alert.put("priority", "MEDIUM");
                    alert.put("color", "#FF8800");
                    break;
                case "LOW_TURNOVER":
                    alert.put("priority", "LOW");
                    alert.put("color", "#FFA500");
                    break;
                default:
                    alert.put("priority", "LOW");
                    alert.put("color", "#888888");
            }
        });
        
        return alerts;
    }

    private BigDecimal calculateInventoryTurnover() {
        BigDecimal cogs = hqDashboardMapper.getCostOfGoodsSold();
        BigDecimal avgInventory = hqDashboardMapper.getAverageInventoryValue();
        
        if (avgInventory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return cogs.divide(avgInventory, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePreviousInventoryTurnover() {
        BigDecimal previousCogs = hqDashboardMapper.getPreviousCostOfGoodsSold();
        BigDecimal previousAvgInventory = hqDashboardMapper.getPreviousAverageInventoryValue();
        
        if (previousAvgInventory.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return previousCogs.divide(previousAvgInventory, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
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
        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + percentage.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}