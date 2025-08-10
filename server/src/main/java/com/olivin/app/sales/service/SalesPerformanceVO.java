package com.olivin.app.sales.service;

import lombok.Data;

/**
 * 매출 실적 조회 결과 VO
 * 
 * 작성자: Claude Code
 * 작성일: 2025.08.10
 * 수정이력:
 * - 2025.08.10 : 최초 작성
 */
@Data
public class SalesPerformanceVO {
    private Long grossSales;        // 총매출
    private Long returns;           // 반품액
    private Long netSales;          // 실매출 (총매출 - 반품액)
    private Long totalOrders;       // 총 주문건수
    private Long averageOrder;      // 평균 주문금액
    private Long totalCustomers;    // 총 고객수
    
    // 전기간 대비 데이터
    private Double grossSalesChange;    // 총매출 변화율
    private Double netSalesChange;      // 실매출 변화율
    private Double ordersChange;        // 주문건수 변화율
}