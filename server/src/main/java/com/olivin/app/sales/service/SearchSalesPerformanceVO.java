package com.olivin.app.sales.service;

import lombok.Data;

/**
 * 매출 실적 조회 검색 조건 VO
 * 
 * 작성자: Claude Code
 * 작성일: 2025.08.10
 * 수정이력:
 * - 2025.08.10 : 최초 작성
 */
@Data
public class SearchSalesPerformanceVO {
    private String fromDate;    // 조회 시작일 (yyyy-MM-dd)
    private String toDate;      // 조회 종료일 (yyyy-MM-dd)
    private String compId;      // 지점 ID (선택사항)
}