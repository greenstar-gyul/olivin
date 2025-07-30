package com.olivin.app.order.mapper;

import java.util.List;

import com.olivin.app.order.service.BranchVO;
import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersVO;

/**
 * 발주서에 관련된 mapper를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 * - 2025.07.30 : 지점정보 조회 추가<br>
 */
public interface OrdersMapper {
	// 지점정보 조회
	public BranchVO selectBranchInfo(String empId);
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
