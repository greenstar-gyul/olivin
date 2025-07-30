package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Type Search Value Object 클래스 <br>
 * 제품 유형 검색에 필요한 데이터 객체를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeSearchVO {
    private String categoryMain;        // 제품 유형 대분류 ID
    private String categoryMainName;   // 제품 유형 대분류명
    private String categorySub;         // 제품 유형 소분류 ID
    private String categorySubName;    // 제품 유형 소분류명
}
