// package com.olivin.app.standard.service;

// import java.util.List;
// import java.util.Map;

// public interface CompanyService {
    
//     List<ProductVO> getAllProducts();
//     List<ProductVO> getApprovedProducts();
//     List<ProductVO> getProductList(ProductVO productVO);
//     ProductVO getProduct(String productId);
//     int saveProduct(ProductVO productVO);
//     int modifyProduct(ProductVO productVO);
//     int removeProduct(String productId);
//     List<ProductVO> searchProducts(Map<String, Object> searchParams);
//     boolean isProductIdExists(String productId);
    
//     int approveProduct(String productId, String approver);
//     int rejectProduct(String productId, String approver, String reason);
    
//     // ✅ 제품 ID 자동생성 메서드 추가
//     String getNextProductId(String categoryMain);
// }