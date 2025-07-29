package com.olivin.app.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.common.service.ProductSearchService;
import com.olivin.app.common.service.ProductSearchVO;

import lombok.RequiredArgsConstructor;

/**
 * 상품 검색 컨트롤러 클래스 <br>
 * 모달창의 검색 영역에서 사용되는 상품 정보 검색 기능을 제공합니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.29
 * 수정이력:
 * - 2025.07.29 : 최초 작성
 */
@RequiredArgsConstructor
@RestController
public class ProductSearchController {
    private final ProductSearchService productSearchService;

    // 전체 상품 목록을 조회하는 API 엔드포인트를 정의합니다.
    // 클라이언트에서 요청한 전체 상품 목록을 반환합니다.
    @GetMapping("/search/products/all")
    public List<ProductSearchVO> selectAllProducts() {
        return productSearchService.selectAllProducts();
    }

    // 상품 검색 API 엔드포인트를 정의합니다.
    // 클라이언트에서 요청한 상품 조건에 맞는 상품 목록을 반환합니다.
    @GetMapping("/search/products")
    public List<ProductSearchVO> searchProducts(@RequestParam String searchValue) {
        return productSearchService.searchProducts(searchValue);
    }
}
