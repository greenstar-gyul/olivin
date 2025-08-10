package com.olivin.app.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.order.service.ApprovalOrdersDTO;
import com.olivin.app.order.service.OrdersDTO;
import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;
import com.olivin.app.order.service.RejectionService;
import com.olivin.app.order.service.RejectionVO;
import com.olivin.app.order.service.SearchOrdersVO;
import com.olivin.app.order.service.UserCompanyVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 발주서에 관련된 컨트롤러 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 * - 2025.07.29 : 상세조회, 등록, 삭제 추가<br>
 * - 2025.07.30 : 지점정보 조회 추가<br>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrdersController {
	private final OrdersService ordersService;
	private final RejectionService rejectionService;
	
	@GetMapping("/orders/user/compInfo")
	public UserCompanyVO getCompInfo(@RequestParam String empId) {
		return ordersService.getCompInfo(empId);
	}
	
	@GetMapping("/orders")
	public List<OrdersVO> ordersList(@ModelAttribute SearchOrdersVO search) {
		log.debug("order = {}", search);
		return ordersService.getAllOrders(search);
	}
	
	@GetMapping("/orders/{orderId}")
	public Map<String, Object> getOrderInfo(@PathVariable String orderId) {
		Map<String, Object> result = new HashMap<>();
		OrdersVO orders = ordersService.getOneOrders(orderId);
		List<OrdersDetailVO> detailList = ordersService.getDetailOrders(orderId);
		if (orders != null) {
			//데이터 정보
			result.put("order", orders);
			result.put("detail", detailList);
		} else {
			result.put("result", "no data");
		}
		return result;
	}
	
	@PostMapping("/orders/{orderId}/approval")
	public Map<String, Object> orderApproval(@PathVariable String orderId, @RequestBody ApprovalOrdersDTO approvalDTO) {
		Map<String, Object> result = new HashMap<>();
		int r = ordersService.approvalOrders(orderId, approvalDTO.getEmployeeId());
		if (r == 1) {
			OrdersVO orderVO = ordersService.getOneOrders(orderId);
			result.put("result", "SUCCESS");
			result.put("message", "성공");
			result.put("data", orderVO);
    } else {
			result.put("result", "FAIL");
			result.put("message", "실패");
    }
  	
		return result;
	}
	
	@PostMapping("/orders/{orderId}/rejection")
	public Map<String, Object> orderRejection(@PathVariable String orderId, @RequestBody RejectionVO rejectionVO) {
		Map<String, Object> result = new HashMap<>();
		int r = ordersService.rejectionOrders(rejectionVO);
		if (r == 1) {
			Map<String, Object> rejectionMap = new HashMap<>();
			OrdersVO orderVO = ordersService.getOneOrders(orderId);
			RejectionVO rejectionOrderVO = rejectionService.getRejectionByOrderId(orderId);
			
			rejectionMap.put("orders", orderVO);
			rejectionMap.put("rejection", rejectionOrderVO);
			
			result.put("result", "SUCCESS");
			result.put("message", "성공");
			result.put("data", rejectionMap);
    } else {
			result.put("result", "FAIL");
			result.put("message", "실패");
    }
//		result.put("result", "TEST");
//		result.put("data", rejectionVO);
  	
		return result;
	}
	
	@GetMapping("/orders/{orderId}/rejection")
	public RejectionVO getOrderRejection(@PathVariable String orderId) {
		return rejectionService.getRejectionByOrderId(orderId);
	}
	
	@GetMapping("/orders/head")
	public List<OrdersVO> orderHeadList(@ModelAttribute SearchOrdersVO search) {
		return ordersService.getAllOrders(search);
	}

	@GetMapping("/orders/branch")
	public List<OrdersVO> orderBranchList(@ModelAttribute SearchOrdersVO search) {
		search.setOrderType("150002");
		return ordersService.getAllOrders(search);
	}
	
	@GetMapping("/orders/supplier")
	public List<OrdersVO> orderSupplierList(@ModelAttribute SearchOrdersVO search) {
		search.setOrderType("150003");
		return ordersService.getAllOrders(search);
	}
	
	@PostMapping("/orders")
	public Map<String, Object> setOrdersList(@RequestBody OrdersDTO orderDTO) {
// @RequestBody Map<String, Object> orderMap 으로 전달 할때
//		OrdersVO ordersVO = objectMapper.convertValue(orderMap.get("order"), OrdersVO.class);
//    List<OrdersDetailVO> detailList = objectMapper.convertValue(orderMap.get("ordersDetail"),
//            new TypeReference<List<OrdersDetailVO>>() {});
		Map<String, Object> result = new HashMap<>();

    log.debug("ORDER : {}", orderDTO.getOrders());
    log.debug("DETAIL : {}", orderDTO.getOrdersDetail());
    if (orderDTO.getOrders() != null) {
    	ordersService.createOrders(orderDTO.getOrders(), orderDTO.getOrdersDetail());
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
	
	@DeleteMapping("/orders/{orderId}")
	public Map<String, Object> deleteOrders(@PathVariable String orderId) {
		Map<String, Object> result = new HashMap<>();
		int r = ordersService.deleteOrder(orderId);
		if (r == 1) {
			result.put("result", "SUCCESS");
			result.put("message", "성공");
    } else {
			result.put("result", "FAIL");
			result.put("message", "실패");
    }
    
    return result;
	}
}
