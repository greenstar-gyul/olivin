package com.olivin.app.order.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 발주서상세에 대한 VO 클래스 <br>
 * 발주서상세를 조회하거나 삽입할때 사용되는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.28<br>
 * 수정이력:<br>
 * - 2025.07.28 : 최초 작성<br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersDetailVO {
	private String poId;
	private String orderId;
	private String productId;
	private String productName;
	private Integer quantity;
	private String unit;
	private String category;
	private String vendorId;
	private String vendorName;
	private Double price;
}
