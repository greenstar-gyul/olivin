package com.olivin.app.common.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olivin.app.common.mapper.ProductTypeSearchMapper;
import com.olivin.app.common.service.ProductTypeSearchService;
import com.olivin.app.common.service.ProductTypeSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Product Type Search Service Implementation <br>
 * 상품 유형 검색 기능을 구현하는 서비스 클래스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@Service
@RequiredArgsConstructor
public class ProductTypeSearchServiceImpl implements ProductTypeSearchService {
    private final ProductTypeSearchMapper productTypeSearchMapper;

    @Override
    public List<ProductTypeSearchVO> getAllProductTypes() {
        return productTypeSearchMapper.selectAllProductTypes();
    }

    @Override
    public List<ProductTypeSearchVO> getProductTypeList(String searchValue) {
        return productTypeSearchMapper.selectProductTypeList(searchValue);
    }

}
