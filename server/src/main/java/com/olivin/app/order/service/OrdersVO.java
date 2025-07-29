package com.olivin.app.order.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrdersVO {
	private String orderId;
	private String orderTitle;
	private String userId;
	private String orderType;
	private String orderFromId;
	private String orderFrom;
	private String orderToId;
	private String orderTo;
	private String reason;
	
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderDate;
  
	private String orderStatus;
	private String approvalUser;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date approvalDate;
	
	private Double totalAmount;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dueDate;
  
	private String remark;
}