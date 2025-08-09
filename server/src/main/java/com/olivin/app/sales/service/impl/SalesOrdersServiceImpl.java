package com.olivin.app.sales.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.sales.mapper.SalesOrdersMapper;
import com.olivin.app.sales.service.SalesDailyClosingVO;
import com.olivin.app.sales.service.SalesOrdersDetailVO;
import com.olivin.app.sales.service.SalesOrdersService;
import com.olivin.app.sales.service.SalesOrdersVO;
import com.olivin.app.sales.service.SearchSalesOrdersVO;

import lombok.RequiredArgsConstructor;

/**
 * 주문서에 관련된 service 인터페이스의 구현 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정자: 함동의<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 * - 2025.08.09 : 일일 정산 서비스 추가<br>
 * @see SalesOrdersService
 */
@Service
@RequiredArgsConstructor
public class SalesOrdersServiceImpl implements SalesOrdersService {
	private final SalesOrdersMapper salesOrdersMapper;
	
	@Override
	public List<SalesOrdersVO> getAllOrders(SearchSalesOrdersVO searchsVO) {
		return salesOrdersMapper.selectAllList(searchsVO);
	}
	
	@Override
	public SalesOrdersVO getOneOrders(String soId) {
		return salesOrdersMapper.selectOneBySoId(soId);
	}
	
	@Override
	public List<SalesOrdersDetailVO> getDetailOrders(String soId) {
		return salesOrdersMapper.selectDetailsBySoId(soId);
	}
	
	@Override
	@Transactional
	public void createSalesOrders(SalesOrdersVO ordersVO, List<SalesOrdersDetailVO> detailVOList) {
		// 1. 메인 주문 정보 등록
		ordersVO.setStatus("020001"); //판매됨
		salesOrdersMapper.insertOne(ordersVO);
		// 2. 상세 주문 정보 리스트 등록
		if (detailVOList != null && !detailVOList.isEmpty()) {
			int count = 0;
			for (SalesOrdersDetailVO detailVO : detailVOList) {    		
				// 3. 주문서 코드 지정
				detailVO.setSodId(ordersVO.getSoId()+String.format("%03d", ++count));
				detailVO.setSoId(ordersVO.getSoId());
				salesOrdersMapper.insertDetailOne(detailVO);
			}
		}
	}

	@Override
	@Transactional
	public void createDailyClosing(SalesDailyClosingVO dailyClosingVO) {
		// 일일 정산 정보를 등록하는 로직을 구현합니다.
		salesOrdersMapper.insertDailyClosing(dailyClosingVO);
	}

	@Override
	public double getDailySummary(SalesDailyClosingVO dailyClosingVO) {
		// 일일 정산 전용 일 매출 조회를 위한 로직을 구현합니다.
		return salesOrdersMapper.selectDailySummary(dailyClosingVO);
	}

	@Override
	public Map<String, Object> getDailySummaryByPaymentType(String compId, String date) {
		// 일일 정산을 위한 결제방식별 매출 집계 조회
		return salesOrdersMapper.selectDailySummaryByPaymentType(compId, date);
	}
}
