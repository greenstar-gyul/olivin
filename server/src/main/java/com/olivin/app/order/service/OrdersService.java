package com.olivin.app.order.service;

import java.util.List;

/**
 * 발주서에 관련된 service를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 * - 2025.07.30 : 지점정보 조회 추가<br>
 */
public interface OrdersService {
	// 회사정보 조회
	UserCompanyVO getCompInfo(String empId);
	//1. 전체조회
	List<OrdersVO> getAllOrders(SearchOrdersVO ordersVO);
	//2. 단건조회
	OrdersVO getOneOrders(String orderId);
	//3. 상세조회
	List<OrdersDetailVO> getDetailOrders(String orderId);
	//4. 발주서 등록
	void createOrders(OrdersVO ordersVO, List<OrdersDetailVO> detailVO);
	//5. 발주서 삭제
	int deleteOrder(String orderId);
}
