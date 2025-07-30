package com.olivin.app.order.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지점을 검색하는 VO 클래스 <br>
 * 회원의 지점에 대한 정보를 가지고 오는 VO입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.30<br>
 * 수정이력:<br>
 * - 2025.07.30 : 최초 작성<br>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchVO {	
	private String employeeId;
	private String compId;
	private String empName;
	private String compName;
	private String compType;
	private String note;
}
