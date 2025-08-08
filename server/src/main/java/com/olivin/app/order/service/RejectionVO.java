package com.olivin.app.order.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 발주서 반려에 대한 VO 클래스 <br>
 * 발주서의 반려를 조회하거나 삽입할때 사용되는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.08<br>
 * 수정이력:<br>
 * - 2025.08.08 : 최초 작성<br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RejectionVO {
	private String rejectionId;
	private String rejectionReson;
	private String rejectionUser;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date rejectionDate;
  
	private String orderId;
}
