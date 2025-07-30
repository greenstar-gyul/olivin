package com.olivin.app.order.service;

import java.util.List;

public interface OrdersService {
	//1. 전체조회
	List<OrdersVO> getAllOrders(OrdersVO ordersVO);
	//2. 단건조회
	OrdersVO getOneOrders(String orderId);
	//3. 상세조회
	List<OrdersDetailVO> getDetailOrders(String orderId);
	//4. 발주서 등록
	void createOrders(OrdersVO ordersVO, List<OrdersDetailVO> detailVO);
	//5. 발주서 삭제
	int deleteOrder(String orderId);
}
