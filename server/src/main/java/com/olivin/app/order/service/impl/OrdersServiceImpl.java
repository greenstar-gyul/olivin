package com.olivin.app.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.order.mapper.OrdersMapper;
import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {
	private final OrdersMapper ordersMapper;
	
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
