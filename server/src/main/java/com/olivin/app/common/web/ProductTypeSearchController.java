package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.common.service.ProductTypeSearchService;
import com.olivin.app.common.service.ProductTypeSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * Product Type Search Controller <br>
 * 상품 유형 검색 기능을 위한 컨트롤러입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductTypeSearchController {
    private final ProductTypeSearchService productTypeSearchService;

    @GetMapping("/search/product-types/all")
    public List<ProductTypeSearchVO> getAllProductTypes() {
        return productTypeSearchService.getAllProductTypes();
    }

    @GetMapping("/search/product-types")
    public List<ProductTypeSearchVO> getProductTypeList(String searchValue) {
        return productTypeSearchService.getProductTypeList(searchValue);
    }
}
