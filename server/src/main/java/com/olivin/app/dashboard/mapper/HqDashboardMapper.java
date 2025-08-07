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
    
    // 🔥 변경: 새로운 KPI 메서드들
    Integer getPendingOutboundCount();              // 출고 대기 건수
    Integer getPreviousPendingOutboundCount();      // 이전 출고 대기 건수
    Integer getPendingPurchaseOrderCount();         // 대기중인 발주서 수
    Integer getPreviousPendingPurchaseOrderCount(); // 이전 대기중인 발주서 수

    // 🔥 삭제: 기존 KPI 메서드들 (더 이상 사용안함)
    /*
    BigDecimal getSupplierDeliveryRate();
    BigDecimal getPreviousSupplierDeliveryRate();
    BigDecimal getStockoutRate();
    BigDecimal getPreviousStockoutRate();
    BigDecimal getCostOfGoodsSold();
    BigDecimal getAverageInventoryValue();
    BigDecimal getPreviousCostOfGoodsSold();
    BigDecimal getPreviousAverageInventoryValue();
    */

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