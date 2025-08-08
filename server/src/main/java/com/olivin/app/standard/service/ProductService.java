package com.olivin.app.standard.service;

import java.util.List;
import java.util.Map;

public interface ProductService {
    
    // 기본 CRUD 메서드들 - 모두 직원 이름 조인 포함
    List<ProductVO> getAllProducts();
    List<ProductVO> getApprovedProducts();
    List<ProductVO> getProductList(ProductVO productVO);
    ProductVO getProduct(String productId);
    int saveProduct(ProductVO productVO);
    int modifyProduct(ProductVO productVO);
    int removeProduct(String productId);
    List<ProductVO> searchProducts(Map<String, Object> searchParams);
    boolean isProductIdExists(String productId);
    
    // 승인 관련 메서드들
    int approveProduct(String productId, String approver);
    int rejectProduct(String productId, String approver, String reason);
    
    // 승인 대기 제품 조회 - 직원 이름 조인 포함
    List<ProductVO> getPendingProducts();
    
    // 제품 ID 자동생성 메서드
    String getNextProductId(String categoryMain);
    
    // ✅ 제품 중단 처리 - 상태를 중단(040004)으로 변경
    int stopProduct(String productId, String updateUser);
}