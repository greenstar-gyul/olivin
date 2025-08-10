package com.olivin.app.sales.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.olivin.app.sales.service.SalesDailyClosingVO;
import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersVO;
import com.olivin.app.sales.service.SearchSalesOrdersVO;

/**
 * 주문서에 관련된 mapper를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정자: 함동의<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 * - 2025.08.09 : 일일 정산 mapper 추가<br>
 */
public interface SalesOrdersMapper {
	//주문서 조회
	public List<SalesOrdersVO> selectAllList(SearchSalesOrdersVO searchVO);
	public SalesOrdersVO selectOneBySoId(String soId);
	public List<SalesOrdersDetailVO> selectDetailsBySoId(String soId);
	//주문서 등록
	public int insertOne(SalesOrdersVO ordersVO);
	public int insertDetailOne(SalesOrdersDetailVO ordersDetailVO);

	// 일일 정산 등록
	public int insertDailyClosing(SalesDailyClosingVO dailyClosingVO);

	// 일일 정산 전용 일 매출 조회
	public double selectDailySummary(SalesDailyClosingVO dailyClosingVO);
	
	// 일일 정산을 위한 결제방식별 매출 집계 조회
	public Map<String, Object> selectDailySummaryByPaymentType(@Param("compId") String compId, @Param("date") String date);
	
	// 일일 정산 상태 확인
	public Map<String, Object> selectDailyClosingStatus(@Param("compId") String compId, @Param("date") String date);
}
