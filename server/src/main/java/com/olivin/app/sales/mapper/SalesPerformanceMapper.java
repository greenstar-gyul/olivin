package com.olivin.app.sales.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.olivin.app.sales.service.SearchSalesPerformanceVO;
import com.olivin.app.sales.service.SalesPerformanceVO;

/**
 * 매출 실적 조회 매퍼
 * 
 * 작성자: Claude Code
 * 작성일: 2025.08.10
 * 수정이력:
 * - 2025.08.10 : 최초 작성
 */
@Mapper
public interface SalesPerformanceMapper {
    
    /**
     * 매출 실적 요약 데이터 조회
     * @param searchVO 검색 조건
     * @return 매출 실적 요약 데이터
     */
    SalesPerformanceVO selectSalesPerformanceSummary(SearchSalesPerformanceVO searchVO);
    
    /**
     * 이전 기간 매출 실적 조회 (비교용)
     * @param searchVO 검색 조건
     * @return 이전 기간 매출 실적 데이터
     */
    SalesPerformanceVO selectPreviousPeriodSummary(SearchSalesPerformanceVO searchVO);
    
    /**
     * 제품별 매출 실적 조회
     * @param searchVO 검색 조건
     * @return 제품별 매출 실적 목록
     */
    List<Map<String, Object>> selectProductSalesPerformance(SearchSalesPerformanceVO searchVO);
    
    /**
     * 지점별 매출 실적 조회
     * @param searchVO 검색 조건
     * @return 지점별 매출 실적 목록
     */
    List<Map<String, Object>> selectBranchSalesPerformance(SearchSalesPerformanceVO searchVO);
}