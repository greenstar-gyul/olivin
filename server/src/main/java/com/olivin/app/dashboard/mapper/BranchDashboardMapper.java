package com.olivin.app.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface BranchDashboardMapper {

    // KPI 관련 쿼리
    /**
     * 지점별 당일 매출
     */
    BigDecimal getTodaySales(@Param("compId") String compId);
    
    /**
     * 지점별 전일 매출 (비교용)
     */
    BigDecimal getYesterdaySales(@Param("compId") String compId);
    
    /**
     * 지점별 월간 매출
     */
    BigDecimal getMonthlySales(@Param("compId") String compId);
    
    /**
     * 지점별 월간 매출 목표
     */
    BigDecimal getMonthlyTarget(@Param("compId") String compId);
    
    /**
     * 지점별 현재 재고 총액
     */
    BigDecimal getCurrentInventoryValue(@Param("compId") String compId);

    // 매출 트렌드 관련
    /**
     * 지점별 최근 7일 매출 트렌드
     */
    List<Map<String, Object>> getWeeklySalesTrend(@Param("compId") String compId);
    
    /**
     * 지점별 카테고리별 매출 구성
     */
    List<Map<String, Object>> getCategorySalesDistribution(@Param("compId") String compId);

    // 재고 관리 관련
    /**
     * 지점별 전체 재고 품목 수
     */
    Integer getTotalInventoryItems(@Param("compId") String compId);
    
    /**
     * 지점별 발주 필요 상품 수 (안전재고 미달)
     */
    Integer getLowStockItemsCount(@Param("compId") String compId);
    
    /**
     * 지점별 품절 상품 수
     */
    Integer getStockoutItemsCount(@Param("compId") String compId);
    
    /**
     * 지점별 발주 필요 상품 리스트
     */
    List<Map<String, Object>> getLowStockItems(@Param("compId") String compId);
    
    /**
     * 지점별 품절 임박 상품 리스트 (안전재고의 20% 이하)
     */
    List<Map<String, Object>> getCriticalStockItems(@Param("compId") String compId);

    // 운영 효율성 관련
    /**
     * 지점별 재고 회전율 계산용 - 매출원가
     */
    BigDecimal getCostOfGoodsSold(@Param("compId") String compId);
    
    /**
     * 지점별 평균 재고 가치
     */
    BigDecimal getAverageInventoryValue(@Param("compId") String compId);
    
    /**
     * 지점별 상위 판매 상품 TOP 5
     */
    List<Map<String, Object>> getTopSellingProducts(@Param("compId") String compId);

    // 알림 관련
    /**
     * 지점별 중요 알림 목록
     */
    List<Map<String, Object>> getBranchAlerts(@Param("compId") String compId);
    
    /**
     * 지점별 당일 거래 건수
     */
    Integer getTodayTransactionCount(@Param("compId") String compId);
    
    /**
     * 지점별 당일 평균 객단가
     */
    BigDecimal getTodayAverageOrderValue(@Param("compId") String compId);
}