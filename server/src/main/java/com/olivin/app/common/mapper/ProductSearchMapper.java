package com.olivin.app.common.mapper;

import java.util.List;

import com.olivin.app.common.service.ProductSearchVO;

/**
 * 상품 검색 Mapper 인터페이스 <br>
 * 모달창의 검색 영역에서 사용되는 상품 정보 Mapper입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.29
 * 수정이력:
 * - 2025.07.29 : 최초 작성
 */
public interface ProductSearchMapper {
    // 전체 상품 목록을 조회합니다.
    List<ProductSearchVO> selectAllProducts();

    // 조건에 맞는 상품 목록을 조회합니다.
    List<ProductSearchVO> selectProductList(String searchValue);
}
