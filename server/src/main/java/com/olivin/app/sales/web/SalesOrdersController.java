package com.olivin.app.sales.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.sales.service.SalesDailyClosingVO;
import com.olivin.app.sales.service.SalesOrdersDTO;
import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersService;
import com.olivin.app.sales.service.SalesOrdersVO;
import com.olivin.app.sales.service.SearchSalesOrdersVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문서에 관련된 컨트롤러 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정자: 함동의<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 * - 2025.08.09 : 일일 정산 API 추가<br>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SalesOrdersController {
	private final SalesOrdersService salesOrdersService;
	
	@GetMapping("/sales/orders")
	public List<SalesOrdersVO> ordersList(
			@RequestParam(value = "soId", required = false) String soId,
			@RequestParam(value = "compName", required = false) String compName,
			@RequestParam(value = "paymentType", required = false) String paymentType,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "empName", required = false) String empName,
			@RequestParam(value = "soDateFrom", required = false) String soDateFrom,
			@RequestParam(value = "soDateTo", required = false) String soDateTo) {
		
		SearchSalesOrdersVO search = SearchSalesOrdersVO.builder()
				.soId(soId)
				.compName(compName)
				.paymentType(paymentType)
				.status(status)
				.empName(empName)
				.soDateFrom(soDateFrom)
				.soDateTo(soDateTo)
				.build();
		
		log.debug("order search parameters = {}", search);
		log.debug("soDateFrom = {}, soDateTo = {}", search.getSoDateFrom(), search.getSoDateTo());
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

	// 일일 정산을 위한 일일 요약 API
	@GetMapping("/sales/daily-summary")
	public Map<String, Object> getDailySummary(
			@RequestParam(value = "compId", required = false) String compId,
			@RequestParam(value = "date", required = false) String date) {
		
		log.debug("Daily summary request - compId: {}, date: {}", compId, date);
		
		Map<String, Object> result = new HashMap<>();
		
		try {
			// 결제 방식별 매출 조회
			Map<String, Object> dailySummary = salesOrdersService.getDailySummaryByPaymentType(compId, date);
			
			// 데이터 매핑 (Oracle의 컬럼명을 camelCase로 변환)
			result.put("totalAmount", dailySummary.get("TOTAL_AMOUNT"));
			result.put("orderCount", dailySummary.get("ORDER_COUNT"));
			result.put("cashAmount", dailySummary.get("CASH_AMOUNT"));
			result.put("transferAmount", dailySummary.get("TRANSFER_AMOUNT"));
			result.put("cardAmount", dailySummary.get("CARD_AMOUNT"));
			
		} catch (Exception e) {
			log.error("Error getting daily summary: ", e);
			result.put("totalAmount", 0);
			result.put("orderCount", 0);
			result.put("cashAmount", 0);
			result.put("transferAmount", 0);
			result.put("cardAmount", 0);
		}
		
		return result;
	}

	@PostMapping("/sales/dailyClosing")
	public Map<String, Object> dailyClosing(@RequestBody SalesDailyClosingVO salesDailyClosingVO) {
		Map<String, Object> result = new HashMap<>();
		
		log.debug("Daily Closing Data: {}", salesDailyClosingVO);
		
		try {
			salesOrdersService.createDailyClosing(salesDailyClosingVO);
			result.put("result", "SUCCESS");
			result.put("message", "일일 정산이 완료되었습니다.");
		} catch (Exception e) {
			log.error("Error during daily closing: ", e);
			result.put("result", "FAIL");
			result.put("message", "일일 정산 중 오류가 발생했습니다.");
		}
		
		return result;
	}
}
