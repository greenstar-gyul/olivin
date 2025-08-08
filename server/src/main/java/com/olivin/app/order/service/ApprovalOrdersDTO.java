package com.olivin.app.order.service;

import com.olivin.app.order.web.OrdersController;

import lombok.Data;

/**
 * ordersController 에서 POST방식으로<br>
 * 발주 승인 데이터를 받을 때 사용되는 DTO 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.08<br>
 * 수정이력:<br>
 * - 2025.08.08 : 최초 작성<br>
 * @see OrdersController
 */
@Data
public class ApprovalOrdersDTO {
	private String employeeId;
	private String orderId;
}
