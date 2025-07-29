package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.ProductSearchMapper;
import com.olivin.app.common.service.ProductSearchService;
import com.olivin.app.common.service.ProductSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * 상품 검색 서비스 구현 클래스 <br>
 * 모달창의 검색 영역에서 사용되는 상품 검색 기능을 구현합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.29
 * 수정이력:
 * - 2025.07.29 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {
    private final ProductSearchMapper productSearchMapper;

    // 전체 상품 목록을 조회합니다.
    @Override
    public List<ProductSearchVO> selectAllProducts() {
        return productSearchMapper.selectAllProducts();
    }

    // 조건에 맞는 상품 목록을 검색합니다.
    @Override
    public List<ProductSearchVO> searchProducts(String searchValue) {
        return productSearchMapper.selectProductList(searchValue);
    }
}
