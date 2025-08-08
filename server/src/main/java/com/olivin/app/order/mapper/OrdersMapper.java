package com.olivin.app.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.olivin.app.order.service.OrdersDetailVO;
import com.olivin.app.order.service.OrdersVO;
import com.olivin.app.order.service.RejectionVO;
import com.olivin.app.order.service.SearchOrdersVO;
import com.olivin.app.order.service.UserCompanyVO;

/**
 * 발주서에 관련된 mapper를 저장되는 인터페이스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 * - 2025.07.30 : 지점정보 조회 추가<br>
 * - 2025.08.08 : 발주서 승인, 반려 추가<br>
 */
public interface OrdersMapper {
	// 회사정보 조회
	public UserCompanyVO selectCompInfo(String empId);
	//1. 전체조회 : (조건조회)
	public List<OrdersVO> selectAllList(SearchOrdersVO ordersVO);
	//2. 단건조회
	public OrdersVO selectOneByOrderId(String orderId);
	//3. 상세조회
	public List<OrdersDetailVO> selectDetailsByOrderId(String orderId);
//	public Map<String, String> selectDetailList(String orderId);
	//4. 발주서 등록
	public int insertOne(OrdersVO ordersVO);
  public int insertDetailOne(OrdersDetailVO detailVO);
  //5. 발주서 승인
  public int ordersApproval(@Param("orderId") String orderId, @Param("empId") String empId);
  //6. 발주서 반려
  public int ordersRejection(String orderId);
  public int ordersRejectionDetail(RejectionVO rejectionVO);
  //8. 발주서 삭제
  public int deleteOrder(String orderId);
  public int deleteOrderDetails(String orderId);
}
