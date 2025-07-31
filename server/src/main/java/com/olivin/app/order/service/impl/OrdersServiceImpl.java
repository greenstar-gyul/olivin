package com.olivin.app.order.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.order.mapper.OrdersMapper;
import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;
import com.olivin.app.order.service.UserCompanyVO;

import lombok.RequiredArgsConstructor;

/**
 * 발주서에 관련된 service 인터페이스의 구현 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 * - 2025.07.29 : insert할때 기본값 추가<br>
 * - 2025.07.30 : 지점정보 조회 추가<br>
 * @see OrdersService
 */
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {
	private final OrdersMapper ordersMapper;
	
	@Override
	public UserCompanyVO getCompInfo(String empId) {
		return ordersMapper.selectCompInfo(empId);
	}
	
	@Override
	public List<OrdersVO> getAllOrders(OrdersVO ordersVO) {
		return ordersMapper.selectAllList(ordersVO);
	}

	@Override
	public OrdersVO getOneOrders(String orderId) {
		return ordersMapper.selectOneByOrderId(orderId);
	}
	
	@Override
	public List<OrdersDetailVO> getDetailOrders(String orderId) {
		return ordersMapper.selectDetailsByOrderId(orderId);
	}
	
	@Override
	@Transactional
	public void createOrders(OrdersVO ordersVO, List<OrdersDetailVO> detailVOList) {
		// 1. 메인 주문 정보 등록
		ordersVO.setOrderDate(new Date()); //SYSDATE
		ordersVO.setOrderStatus("03001");
    ordersMapper.insertOne(ordersVO);
    // 2. 상세 주문 정보 리스트 등록
    if (detailVOList != null && !detailVOList.isEmpty()) {
    	int count = 0;
    	for (OrdersDetailVO detailVO : detailVOList) {
    		detailVO.setPoId(ordersVO.getOrderId()+String.format("%03d", ++count));
    		detailVO.setOrderId(ordersVO.getOrderId());
    		ordersMapper.insertDetailOne(detailVO);
    	}
    }
	}
	
	@Override
  @Transactional
  public int deleteOrder(String orderId) {
		// 1. 해당 주문과 관련된 상세 주문들 먼저 삭제
		ordersMapper.deleteOrderDetails(orderId);
		// 2. 메인 주문 삭제
		return ordersMapper.deleteOrder(orderId);
  }
}
