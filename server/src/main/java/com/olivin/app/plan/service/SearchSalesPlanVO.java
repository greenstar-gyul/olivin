package com.olivin.app.plan.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 매출계획에 검색에 대한 VO 클래스 <br>
 * 매출게획을 조회할때 사용되는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.08.04<br>
 * 수정이력:<br>
 * - 2025.08.04 : 최초 작성<br>
 * - 2025.08.05 : 사용자이름 추가<br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchSalesPlanVO {
	private String planId;
	private Integer year;
	private Integer quarter;
	private String compId;
	private String compName;
	private Double targetAmount;
	private String regUser;
	private String regUserName;
	private Date regDate;
	private String updateUser;
	private String updateUserName;
	private Date updateDate;
	private String reason;
	
	//검색할때 사용되는 변수
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date regDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date regDateTo;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date updateDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date updateDateTo;
}
