package com.olivin.app.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplierDashboardMapper {

    /**
     * 공급업체 목록 조회 (시스템 관리자용)
     */
    List<Map<String, Object>> getAvailableSuppliers();

    /**
     * 공급업체 정보 조회
     */
    Map<String, Object> getSupplierInfo(String supplierId);

    /**
     * 월간 수주량 조회 (현재 월)
     */
    Integer getMonthlyOrderCount(String supplierId);

    /**
     * 월간 수주량 조회 (지난 월)
     */
    Integer getLastMonthOrderCount(String supplierId);

    /**
     * 월간 수주 금액 조회 (이번 달)
     */
    Long getMonthlyOrderAmount(String supplierId);

    /**
     * 월간 수주 금액 조회 (지난 달)
     */
    Long getLastMonthOrderAmount(String supplierId);

    /**
     * 주문 처리율 데이터 조회 (현재 월)
     */
    Map<String, Object> getOrderCompletionRate(String supplierId);

    /**
     * 주문 처리율 데이터 조회 (지난 월)
     */
    Map<String, Object> getLastMonthCompletionRate(String supplierId);

    /**
     * 평균 배송 시간 조회 (현재 월)
     */
    Double getAverageDeliveryTime(String supplierId);

    /**
     * 평균 배송 시간 조회 (지난 월)
     */
    Double getLastMonthAverageDeliveryTime(String supplierId);

    /**
     * 이번 달 발주 건수 조회 - 본사→공급업체 + 공급업체→본사 승인건수
     */
    Double getQualityScore(String supplierId);

    /**
     * 지난 달 발주 건수 조회 - 본사→공급업체 + 공급업체→본사 승인건수
     */
    Double getLastMonthQualityScore(String supplierId);

    /**
     * 진행 중인 발주 현황 조회
     */
    List<Map<String, Object>> getActiveOrders(String supplierId);

    /**
     * 상위 공급 상품 조회 (TOP 10)
     */
    List<Map<String, Object>> getTopProducts(String supplierId);

    /**
     * 월간 수주 트렌드 조회 (최근 6개월)
     */
    List<Map<String, Object>> getOrderTrend(String supplierId);

    /**
     * 카테고리별 공급 비중 조회 (현재 월)
     */
    List<Map<String, Object>> getCategorySupply(String supplierId);

    /**
     * 품질/배송 관련 알림 조회
     */
    List<Map<String, Object>> getAlerts(String supplierId);

    /**
     * 모든 주문 상태 조회 (디버깅용)
     */
    List<Map<String, Object>> getAllOrderStatus(String supplierId);

    /**
     * 공급업체별 발주 데이터 디버깅 정보 조회
     */
    List<Map<String, Object>> getSupplierOrderDebugInfo(String supplierId);
}