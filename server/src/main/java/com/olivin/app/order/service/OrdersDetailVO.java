package com.olivin.app.order.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private String status;
	private String category;
	private String vendorId;
	private String vendorName;
	private Double price;
}
