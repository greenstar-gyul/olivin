package com.olivin.app.sales.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.olivin.app.sales.mapper.SalesPerformanceMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매출 실적 조회 서비스
 * 
 * 작성자: Claude Code
 * 작성일: 2025.08.10
 * 수정이력:
 * - 2025.08.10 : 최초 작성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesPerformanceService {
    
    private final SalesPerformanceMapper salesPerformanceMapper;
    
    /**
     * 매출 실적 요약 데이터 조회
     * @param searchVO 검색 조건
     * @return 매출 실적 요약 데이터 (전기간 대비 변화율 포함)
     */
    public SalesPerformanceVO getSalesPerformanceSummary(SearchSalesPerformanceVO searchVO) {
        log.debug("매출 실적 조회 시작 - 조건: {}", searchVO);
        
        // 현재 기간 매출 실적 조회
        SalesPerformanceVO currentPeriod = salesPerformanceMapper.selectSalesPerformanceSummary(searchVO);
        if (currentPeriod == null) {
            log.warn("현재 기간 매출 데이터가 없습니다. 기본값으로 초기화합니다.");
            currentPeriod = new SalesPerformanceVO();
            currentPeriod.setGrossSales(0L);
            currentPeriod.setReturns(0L);
            currentPeriod.setNetSales(0L);
            currentPeriod.setTotalOrders(0L);
            currentPeriod.setAverageOrder(0L);
            currentPeriod.setTotalCustomers(0L);
        }
        
        // 이전 기간 매출 실적 조회
        SalesPerformanceVO previousPeriod = salesPerformanceMapper.selectPreviousPeriodSummary(searchVO);
        if (previousPeriod == null) {
            log.warn("이전 기간 매출 데이터가 없습니다. 변화율은 0으로 설정합니다.");
            previousPeriod = new SalesPerformanceVO();
            previousPeriod.setGrossSales(0L);
            previousPeriod.setNetSales(0L);
            previousPeriod.setTotalOrders(0L);
        }
        
        // 전기간 대비 변화율 계산
        double grossSalesChange = calculateChangeRate(previousPeriod.getGrossSales(), currentPeriod.getGrossSales());
        double netSalesChange = calculateChangeRate(previousPeriod.getNetSales(), currentPeriod.getNetSales());
        double ordersChange = calculateChangeRate(previousPeriod.getTotalOrders(), currentPeriod.getTotalOrders());
        
        currentPeriod.setGrossSalesChange(Math.round(grossSalesChange * 10.0) / 10.0);
        currentPeriod.setNetSalesChange(Math.round(netSalesChange * 10.0) / 10.0);
        currentPeriod.setOrdersChange(Math.round(ordersChange * 10.0) / 10.0);
        
        log.debug("매출 실적 조회 완료 - 결과: {}", currentPeriod);
        return currentPeriod;
    }
    
    /**
     * 변화율 계산
     * @param previousValue 이전 값
     * @param currentValue 현재 값
     * @return 변화율 (%)
     */
    private double calculateChangeRate(Long previousValue, Long currentValue) {
        if (previousValue == null || previousValue == 0) {
            return currentValue != null && currentValue > 0 ? 100.0 : 0.0;
        }
        
        if (currentValue == null) {
            return -100.0;
        }
        
        return ((currentValue.doubleValue() - previousValue.doubleValue()) / previousValue.doubleValue()) * 100.0;
    }
    
    /**
     * 제품별 매출 실적 조회
     * @param searchVO 검색 조건
     * @return 제품별 매출 실적 목록
     */
    public List<Map<String, Object>> getProductSalesPerformance(SearchSalesPerformanceVO searchVO) {
        log.debug("제품별 매출 실적 조회 시작 - 조건: {}", searchVO);
        
        List<Map<String, Object>> result = salesPerformanceMapper.selectProductSalesPerformance(searchVO);
        
        log.debug("제품별 매출 실적 조회 완료 - 결과 건수: {}", result != null ? result.size() : 0);
        return result != null ? result : new ArrayList<>();
    }
    
    /**
     * 지점별 매출 실적 조회
     * @param searchVO 검색 조건
     * @return 지점별 매출 실적 목록
     */
    public List<Map<String, Object>> getBranchSalesPerformance(SearchSalesPerformanceVO searchVO) {
        log.debug("지점별 매출 실적 조회 시작 - 조건: {}", searchVO);
        
        List<Map<String, Object>> result = salesPerformanceMapper.selectBranchSalesPerformance(searchVO);
        
        log.debug("지점별 매출 실적 조회 완료 - 결과 건수: {}", result != null ? result.size() : 0);
        return result != null ? result : new ArrayList<>();
    }
}