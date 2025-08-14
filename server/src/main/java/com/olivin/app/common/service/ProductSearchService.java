package com.olivin.app.common.service;

import java.util.List;

/**
 * 상품 검색 서비스 인터페이스 <br>
 * 모달창의 검색 영역에서 사용되는 상품 검색 기능 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.29
 * 수정이력:
 * - 2025.07.29 : 최초 작성
 */
public interface ProductSearchService {
    // 전체 상품 목록을 조회합니다.
    List<ProductSearchVO> selectAllProducts();

    // 조건에 맞는 상품 목록을 검색합니다.
    public List<ProductSearchVO> searchProducts(String searchValue);

    // 공급업체별 전체 상품 목록을 조회합니다.
    List<ProductSearchVO> selectProductsByVendor(String vendorName);

    // 공급업체별 상품 목록을 검색합니다.
    List<ProductSearchVO> searchProductsByVendor(String searchValue, String vendorName);
}
