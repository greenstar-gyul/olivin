package com.olivin.app.order.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 발주서 검색에 대한 VO 클래스 <br>
 * 발주서를 조회할때 사용되는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.01<br>
 * 수정이력:<br>
 * - 2025.08.01 : 최초 작성<br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchOrdersVO {
	private String orderId;
	private String orderTitle;
	private String creatorId;
	private String creatorName;
	private String orderType;
	private String orderTypeName;
	private String orderFromId;
	private String orderFrom;
	private String orderToId;
	private String orderTo;
	private String reason;
	private String reasonName;
	
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date orderDate;
  
	private String orderStatus;
	private String orderStatusName;
	private String approverId;
	private String approverName;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date approvalDate;
	
	private Double totalAmount;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date dueDate;
  
	private String remark;
	
	//검색할때 사용되는 변수
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date orderDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date orderDateTo;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date dueDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date dueDateTo;
}
