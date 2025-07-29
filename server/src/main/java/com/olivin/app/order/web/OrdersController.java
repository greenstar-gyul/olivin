package com.olivin.app.order.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrdersController {
	private final OrdersService ordersService;
	
	@GetMapping("/orders")
	public List<OrdersVO> ordersList() {
		return ordersService.getAllOrders(null);
	}
	
	@GetMapping("/orders/head")
	public List<OrdersVO> orderHeadList() {
		OrdersVO ordersVO = OrdersVO.builder()
				.orderType("150001")
				.build();
		return ordersService.getAllOrders(ordersVO);
	}
	
	@GetMapping("/orders/branch")
	public List<OrdersVO> orderBranchList() {
		OrdersVO ordersVO = OrdersVO.builder()
				.orderType("150002")
				.build();
		return ordersService.getAllOrders(ordersVO);
	}
	
	@GetMapping("/orders/supplier")
	public List<OrdersVO> orderSupplierList() {
		OrdersVO ordersVO = OrdersVO.builder()
				.orderType("150003")
				.build();
		return ordersService.getAllOrders(ordersVO);
	}
}
