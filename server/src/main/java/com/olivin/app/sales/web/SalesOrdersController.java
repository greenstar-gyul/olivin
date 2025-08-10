package com.olivin.app.sales.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.sales.service.SalesOrdersDTO;
import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersService;
import com.olivin.app.sales.service.SalesOrdersVO;
import com.olivin.app.sales.service.SearchSalesOrdersVO;
import com.olivin.app.sales.service.SalesPerformanceService;
import com.olivin.app.sales.service.SalesPerformanceVO;
import com.olivin.app.sales.service.SearchSalesPerformanceVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문서에 관련된 컨트롤러 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SalesOrdersController {
	private final SalesOrdersService salesOrdersService;
	private final SalesPerformanceService salesPerformanceService;
	
	@GetMapping("/sales/orders")
	public List<SalesOrdersVO> ordersList(@ModelAttribute SearchSalesOrdersVO search) {
		log.debug("order = {}", search);
		return salesOrdersService.getAllOrders(search);
	}
	
	@GetMapping("sales/orders/{osId}")
	public Map<String, Object> getOrderInfo(@PathVariable String osId) {
		Map<String, Object> result = new HashMap<>();
		SalesOrdersVO orders = salesOrdersService.getOneOrders(osId);
		List<SalesOrdersDetailVO> detailList = salesOrdersService.getDetailOrders(osId);
		if (orders != null) {
			//데이터 정보
			result.put("order", orders);
			result.put("detail", detailList);
		} else {
			result.put("result", "no data");
		}
		return result;
	}
	
	@PostMapping("/sales/orders")
	public Map<String, Object> setOrdersList(@RequestBody SalesOrdersDTO orderDTO) {
		Map<String, Object> result = new HashMap<>();
    
    log.debug("SALES_ORDER : {}", orderDTO.getOrders());
    log.debug("DETAIL : {}", orderDTO.getOrdersDetail());
    if (orderDTO.getOrders() != null) {
    	salesOrdersService.createSalesOrders(orderDTO.getOrders(), orderDTO.getOrdersDetail());
    	result.put("result", "SUCCESS");
			result.put("message", "성공");
    } else {
			result.put("result", "FAIL");
			result.put("message", "실패");
    }
//    result.put("result", "TEST");
//    result.put("data", orderDTO);
	    
	    return result;
	}
	
	/**
	 * 매출 실적 요약 조회
	 * @param fromDate 시작일 (yyyy-MM-dd)
	 * @param toDate 종료일 (yyyy-MM-dd) 
	 * @param compId 지점 ID (optional)
	 * @return 매출 실적 요약 데이터
	 */
	@GetMapping("/sales/performance/summary")
	public Map<String, Object> getSalesPerformanceSummary(
			@RequestParam String fromDate,
			@RequestParam String toDate,
			@RequestParam(required = false) String compId) {
		
		log.debug("매출 실적 조회 - fromDate: {}, toDate: {}, compId: {}", fromDate, toDate, compId);
		
		Map<String, Object> result = new HashMap<>();
		
		try {
			// 검색 조건 설정
			SearchSalesPerformanceVO searchVO = new SearchSalesPerformanceVO();
			searchVO.setFromDate(fromDate);
			searchVO.setToDate(toDate);
			searchVO.setCompId(compId);
			
			// 실제 DB에서 매출 실적 데이터 조회
			SalesPerformanceVO performanceData = salesPerformanceService.getSalesPerformanceSummary(searchVO);
			
			// 결과 매핑
			result.put("grossSales", performanceData.getGrossSales());
			result.put("returns", performanceData.getReturns());
			result.put("netSales", performanceData.getNetSales());
			result.put("totalOrders", performanceData.getTotalOrders());
			result.put("averageOrder", performanceData.getAverageOrder());
			result.put("totalCustomers", performanceData.getTotalCustomers());
			
			// 전기간 대비 데이터
			Map<String, Object> compareData = new HashMap<>();
			compareData.put("grossSalesChange", performanceData.getGrossSalesChange());
			compareData.put("netSalesChange", performanceData.getNetSalesChange());
			compareData.put("ordersChange", performanceData.getOrdersChange());
			result.put("compareData", compareData);
			
		} catch (Exception e) {
			log.error("매출 실적 조회 중 오류 발생", e);
			result.put("error", "매출 실적 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 제품별 매출 실적 조회
	 * @param fromDate 시작일 (yyyy-MM-dd)
	 * @param toDate 종료일 (yyyy-MM-dd) 
	 * @param compId 지점 ID
	 * @return 제품별 매출 실적 목록
	 */
	@GetMapping("/sales/performance/products")
	public List<Map<String, Object>> getProductSalesPerformance(
			@RequestParam String fromDate,
			@RequestParam String toDate,
			@RequestParam(required = false) String compId) {
		
		log.debug("제품별 매출 실적 조회 - fromDate: {}, toDate: {}, compId: {}", fromDate, toDate, compId);
		
		try {
			SearchSalesPerformanceVO searchVO = new SearchSalesPerformanceVO();
			searchVO.setFromDate(fromDate);
			searchVO.setToDate(toDate);
			searchVO.setCompId(compId);
			
			return salesPerformanceService.getProductSalesPerformance(searchVO);
			
		} catch (Exception e) {
			log.error("제품별 매출 실적 조회 중 오류 발생", e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * 지점별 매출 실적 조회
	 * @param fromDate 시작일 (yyyy-MM-dd)
	 * @param toDate 종료일 (yyyy-MM-dd)
	 * @return 지점별 매출 실적 목록
	 */
	@GetMapping("/sales/performance/branches")
	public List<Map<String, Object>> getBranchSalesPerformance(
			@RequestParam String fromDate,
			@RequestParam String toDate) {
		
		log.debug("지점별 매출 실적 조회 - fromDate: {}, toDate: {}", fromDate, toDate);
		
		try {
			SearchSalesPerformanceVO searchVO = new SearchSalesPerformanceVO();
			searchVO.setFromDate(fromDate);
			searchVO.setToDate(toDate);
			
			return salesPerformanceService.getBranchSalesPerformance(searchVO);
			
		} catch (Exception e) {
			log.error("지점별 매출 실적 조회 중 오류 발생", e);
			return new ArrayList<>();
		}
	}
}
