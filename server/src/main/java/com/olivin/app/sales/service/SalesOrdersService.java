package com.olivin.app.sales.service;

import java.util.List;
import java.util.Map;

/**
 * 주문서에 관련된 service를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정자: 함동의<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 * - 2025.08.09 : 일일 정산 서비스 추가<br>
 */
public interface SalesOrdersService {
	//주문서 조회
	public List<SalesOrdersVO> getAllOrders(SearchSalesOrdersVO searchVO);
	public SalesOrdersVO getOneOrders(String orderId);
	public List<SalesOrdersDetailVO> getDetailOrders(String orderId);
	//주문서 등록
	public void createSalesOrders(SalesOrdersVO ordersVO, List<SalesOrdersDetailVO> detailVOList);

	// 일일 정산 등록
	public void createDailyClosing(SalesDailyClosingVO dailyClosingVO);

	// 일일 정산 전용 일 매출 조회
	public double getDailySummary(SalesDailyClosingVO dailyClosingVO);
	
	// 일일 정산을 위한 결제방식별 매출 집계 조회
	public Map<String, Object> getDailySummaryByPaymentType(String compId, String date);
}
