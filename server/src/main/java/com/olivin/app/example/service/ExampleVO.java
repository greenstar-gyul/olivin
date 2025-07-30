package com.olivin.app.example.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Example VO 클래스 <br>
 * 예제 데이터 객체입니다. <br>
 * <br>
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.24 <br>
 * 수정이력: <br>
 * 2025.07.24 : 최초 작성 <br>
 * 2025.07.29 : 설명 추가 <br>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleVO {
    private String productId;                   // 상품 ID
    private String productName;                 // 상품명
    private String categoryMain;                // 대분류 카테고리
    private String categorySub;                 // 소분류 카테고리
    private String vendorName;                  // 공급사명
    private String productSpec;                 // 상품 규격
    private String unit;                        // 단위
    private Integer packQty;                    // 포장 수량
    private Integer safetyStock;                // 안전 재고량
    private Double purchasePrice;               // 구매가
    private Double sellPrice;                   // 소비자 판매가
    private String regUser;                     // 등록자

    @DateTimeFormat(pattern = "yyyy-MM-dd")     // 날짜 패턴 지정
    private Date regDate;                       // 등록일

    private String updateUser;                  // 수정자

    @DateTimeFormat(pattern = "yyyy-MM-dd")     // 날짜 패턴 지정
    private Date updateDate;                    // 수정일

    private String status;                      // 등록 상태
    private String productImage;                // 상품 이미지 URL
    private String note;                        // 비고
}
