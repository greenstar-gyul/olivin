package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 검색 VO 클래스 <br>
 * 모달창의 검색 영역에서 사용되는 상품 정보 VO입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.29
 * 수정이력:
 * - 2025.07.29 : 최초 작성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchVO {
    private String productId;       // 상품 ID
    private String productName;     // 상품명
    private String categoryMain;    // 대분류 카테고리
    private String categorySub;     // 소분류 카테고리
    private String vendorName;      // 공급사명
    private String productSpec;     // 상품 규격
}
