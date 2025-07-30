package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회사 검색 VO 클래스 <br>
 * 모달창의 검색 영역에서 사용되는 회사 정보 VO입니다.<br>
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
public class CompanySearchVO {
	private String compId;
	private String compName;
	private String compType;
	private String ceoName;
	private String phone;
	private String address;
	private String settleMgr;
	private String note;
}
