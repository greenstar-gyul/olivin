package com.olivin.app.sales.service;

import java.util.List;

/**
 * 주문서에 관련된 service를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 */
public interface SalesOrdersService {
	//주문서 조회
	List<SalesOrdersVO> getAllOrders(SearchSalesOrdersVO searchVO);
	SalesOrdersVO getOneOrders(String orderId);
	List<SalesOrdersDetailVO> getDetailOrders(String orderId);
	//주문서 등록
	void createSalesOrders(SalesOrdersVO ordersVO, List<SalesOrdersDetailVO> detailVOList);
}
