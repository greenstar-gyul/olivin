package com.olivin.app.order.mapper;

import java.util.List;
import java.util.Map;

import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersVO;

public interface OrdersMapper {
	//1. 전체조회 : (조건조회)
	public List<OrdersVO> selectAllList(OrdersVO ordersVO);
	//2. 단건조회
	public OrdersVO selectOneByOrderId(String orderId);
	//3. 상세조회
	public List<OrdersDetailVO> selectDetailsByOrderId(String orderId);
//	public Map<String, String> selectDetailList(String orderId);
	//4. 발주서 등록
	public int insertOne(OrdersVO ordersVO);
  public int insertDetailOne(OrdersDetailVO detailVO);
  //5. 발주서 삭제
  public int deleteOrder(String orderId);
  public int deleteOrderDetails(String orderId);
}
