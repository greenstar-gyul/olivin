package com.olivin.app.product.service;

import java.util.List;
import java.util.Map;

public interface ProductService {
    
    List<ProductVO> getAllProducts();
    List<ProductVO> getApprovedProducts();
    List<ProductVO> getProductList(ProductVO productVO);
    ProductVO getProduct(String productId);
    int saveProduct(ProductVO productVO);
    int modifyProduct(ProductVO productVO);
    int removeProduct(String productId);
    List<ProductVO> searchProducts(Map<String, Object> searchParams);
    boolean isProductIdExists(String productId);
    
    int approveProduct(String productId, String approver);
    int rejectProduct(String productId, String approver, String reason);
}