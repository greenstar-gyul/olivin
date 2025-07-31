package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company Search Value Object <br>
 * 본사/지점의 재고를 조회하기 위해
 * 회사 검색 기능을 위한 VO 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchCompanySearchVO {
    private String compId;      // 회사 ID
    private String compName;    // 회사명
    private String phone;      // 전화번호
    private String note;        // 비고
}
