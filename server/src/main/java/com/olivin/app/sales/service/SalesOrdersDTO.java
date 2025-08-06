package com.olivin.app.sales.service;

import java.util.List;

import lombok.Data;

/**
 * SalesOrdersController 에서 POST방식으로<br>
 * 데이터를 받을 때 사용되는 DTO 클래스<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 * @see SalesOrdersController
 */
@Data
public class SalesOrdersDTO {
	private SalesOrdersVO orders;
	private List<SalesOrdersDetailVO> ordersDetail;
}
