package com.olivin.app.sales.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문서 상세에 대한 VO 클래스 <br>
 * 주문서 상세를 조회하거나 삽입할때 사용되는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.06<br>
 * 수정이력:<br>
 * - 2025.08.06 : 최초 작성<br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrdersDetailVO {
	private String sodId;
	private String productId;
	private String productName;
	private String unitPrice;
	private Integer quantity;
	private Integer price;
	private String soId;
}
