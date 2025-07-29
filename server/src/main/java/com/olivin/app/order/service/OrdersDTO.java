package com.olivin.app.order.service;

import java.util.List;

import com.olivin.app.order.web.OrdersController;

import lombok.Data;

/**
 * {@code ordersController}에서 POST방식으로<br>
 * 데이터를 받을 때 사용되는 클래스
 * 
 * @author 이창현
 * @see OrdersController
 */
@Data
public class OrdersDTO {
	OrdersVO orders;
	List<OrdersDetailVO> ordersDetail;
}
