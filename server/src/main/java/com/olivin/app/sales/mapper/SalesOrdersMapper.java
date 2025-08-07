package com.olivin.app.sales.mapper;

import java.util.List;

import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersVO;
import com.olivin.app.sales.service.SearchSalesOrdersVO;

/**
 * 주문서에 관련된 mapper를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 */
public interface SalesOrdersMapper {
	//주문서 조회
	public List<SalesOrdersVO> selectAllList(SearchSalesOrdersVO searchVO);
	public SalesOrdersVO selectOneBySoId(String soId);
	public List<SalesOrdersDetailVO> selectDetailsBySoId(String soId);
	//주문서 등록
	public int insertOne(SalesOrdersVO ordersVO);
	public int insertDetailOne(SalesOrdersDetailVO ordersDetailVO);
}
