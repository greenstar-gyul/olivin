package com.olivin.app.sales.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문서에 대한 VO 클래스 <br>
 * 주문서를 조회하거나 삽입할때 사용되는 VO입니다.<br>
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
public class SalesOrdersVO {
	private String soId;
	private String compId;
	private String compName;
	private String paymentType;
	private String paymentTypeName;
	private String status;
	private String statusName;
	private Integer totalPrice;
	private String empId;
	private String empName;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date soDate;
}
