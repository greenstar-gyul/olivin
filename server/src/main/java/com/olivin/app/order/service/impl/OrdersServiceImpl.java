package com.olivin.app.order.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.order.mapper.OrdersMapper;
import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersService;
import com.olivin.app.order.service.OrdersVO;
import com.olivin.app.order.service.RejectionVO;
import com.olivin.app.order.service.SearchOrdersVO;
import com.olivin.app.order.service.UserCompanyVO;
import com.olivin.app.standard.service.ProductService;
import com.olivin.app.standard.service.ProductVO;

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
 * - 2025.08.08 : 발주서 승인, 반려 추가<br>
 * @see OrdersService
 */
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl implements OrdersService {
	private final OrdersMapper ordersMapper;
	private final ProductService productService;
	
	@Override
	public UserCompanyVO getCompInfo(String empId) {
		return ordersMapper.selectCompInfo(empId);
	}
	
	@Override
	public List<OrdersVO> getAllOrders(SearchOrdersVO ordersVO) {
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
		ordersVO.setOrderStatus("030001"); //승인대기
    ordersMapper.insertOne(ordersVO);
    // 2. 상세 주문 정보 리스트 등록
    if (detailVOList != null && !detailVOList.isEmpty()) {
    	int count = 0;
    	//category, vendorId, vendorName, 가지고 오기.
    	for (OrdersDetailVO detailVO : detailVOList) {
    		// 3. 제품 정보 추가
    		ProductVO productVO = productService.getProduct(detailVO.getProductId());
    		detailVO.setCategory(productVO.getCategoryMain());
    		detailVO.setVendorId(productVO.getCompId());
    		detailVO.setVendorName(productVO.getVendorName());
    		
    		// 4. 발주서 코드 지정
    		detailVO.setPoId(ordersVO.getOrderId()+String.format("%03d", ++count));
    		detailVO.setOrderId(ordersVO.getOrderId());
    		ordersMapper.insertDetailOne(detailVO);
    	}
    }
	}
	
	@Override
	public int approvalOrders(String orderId, String empId) {
		return ordersMapper.ordersApproval(orderId, empId);
	}
	
	@Override
	@Transactional
	public int rejectionOrders(RejectionVO rejectionVO) {
		// 1. 발주반려 테이블 추가
		ordersMapper.ordersRejectionDetail(rejectionVO);
		// 2. 발주서 상태를 '반려'로 변경
		return ordersMapper.ordersRejection(rejectionVO.getOrderId());
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
