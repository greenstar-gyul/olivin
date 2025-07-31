package com.olivin.app.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vendor Search Value Object 클래스 <br>
 * 공급업체 검색에 필요한 데이터 객체를 정의합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorSearchVO {
    private String vendorId;      // 업체 ID
    private String vendorName;    // 업체명
    private String phone;       // 전화번호
}
