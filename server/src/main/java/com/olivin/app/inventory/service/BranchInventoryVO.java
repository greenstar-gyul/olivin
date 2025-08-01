package com.olivin.app.inventory.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BranchInventory Value Object 클래스 <br>
 * 지점 재고 관리에 필요한 데이터 객체를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.31
 * 수정이력:
 * 2025.07.31 : 최초 작성
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchInventoryVO {
    private String productId; // 제품 ID
    private String productName; // 제품 이름
    private String categoryMain; // 대분류
    private String categorySub; // 소분류
    private String vendorName; // 공급사 이름
    private String productSpec; // 제품 규격
    private int stockQuantity; // 재고 수량
    private int safetyStock; // 안전 재고
    private String compName; // 지점명
}
