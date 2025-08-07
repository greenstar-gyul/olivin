package com.olivin.app.sales.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.order.service.OrdersDTO;
import com.olivin.app.plan.service.SalesPlanService;
import com.olivin.app.plan.web.SalesPlanController;
import com.olivin.app.sales.service.SalesOrdersDTO;
import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersService;
import com.olivin.app.sales.service.SalesOrdersVO;

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
	
	@GetMapping("/sales/orders")
	public List<SalesOrdersVO> ordersList(@ModelAttribute SalesOrdersVO search) {
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
}
