package com.olivin.app.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface HqDashboardMapper {

    // KPI 관련 쿼리
    BigDecimal getMonthlyRevenue();
    BigDecimal getPreviousMonthRevenue();
    BigDecimal getSupplierDeliveryRate();
    BigDecimal getPreviousSupplierDeliveryRate();
    BigDecimal getStockoutRate();
    BigDecimal getPreviousStockoutRate();

    // 재고 회전율 계산용
    BigDecimal getCostOfGoodsSold();
    BigDecimal getAverageInventoryValue();
    BigDecimal getPreviousCostOfGoodsSold();
    BigDecimal getPreviousAverageInventoryValue();

    // 매출 트렌드 관련
    List<Map<String, Object>> getSalesTrendByCategory();
    List<Map<String, Object>> getCategorySales();

    // 공급업체 관련
    List<Map<String, Object>> getTopSuppliers();

    // 재고 관련
    Integer getTotalInventoryItems();
    Integer getLowStockItems();
    Integer getStockoutItems();
    BigDecimal getTotalInventoryValue();
    List<Map<String, Object>> getInventoryByCategory();

    // 물류센터 효율성 관련
    BigDecimal getAverageProcessingTime();
    BigDecimal getSameDayProcessingRate();
    BigDecimal getWarehouseUtilization();
    List<Map<String, Object>> getDailyThroughputTrend();
    List<Map<String, Object>> getEfficiencyMetrics();

    // 알림 관련
    List<Map<String, Object>> getCriticalAlerts();
}