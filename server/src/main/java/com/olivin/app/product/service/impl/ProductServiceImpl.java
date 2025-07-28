package com.olivin.app.product.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.product.mapper.ProductMapper;
import com.olivin.app.product.service.ProductService;
import com.olivin.app.product.service.ProductVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    
    // 상태 코드 상수 정의 (공통코드 테이블 기준)
    private static final String STATUS_APPROVED = "ST001";  // 정상 (승인완료)
    private static final String STATUS_PENDING = "ST002";   // 승인 (승인대기)
    private static final String STATUS_WAITING = "ST003";   // 대기
    private static final String STATUS_HOLD = "ST004";      // 보류 상태

    @Override
    public List<ProductVO> getAllProducts() {
        return productMapper.selectAllList();
    }
    
    @Override
    public List<ProductVO> getApprovedProducts() {
        ProductVO searchVO = new ProductVO();
        searchVO.setStatus(STATUS_APPROVED);  // "ST001" 사용
        return productMapper.selectProductList(searchVO);
    }
    
    @Override
    public List<ProductVO> getProductList(ProductVO productVO) {
        return productMapper.selectProductList(productVO);
    }
    
    @Override
    public ProductVO getProduct(String productId) {
        return productMapper.selectProduct(productId);
    }
    
    @Override
    public int saveProduct(ProductVO productVO) {
        if (productVO.getProductId() == null || productVO.getProductId().isEmpty()) {
            String newProductId = generateProductId();
            productVO.setProductId(newProductId);
        }
        
        if (productVO.getStatus() == null || productVO.getStatus().isEmpty()) {
            productVO.setStatus(STATUS_PENDING);  // "ST002" 사용
        }
        
        productVO.setRegDate(new Date());
        
        if (productVO.getRegUser() == null || productVO.getRegUser().isEmpty()) {
            productVO.setRegUser("SYSTEM");
        }
        
        return productMapper.insertProduct(productVO);
    }
    
    @Override
    public int modifyProduct(ProductVO productVO) {
        productVO.setUpdateDate(new Date());
        return productMapper.updateProduct(productVO);
    }
    
    @Override
    public int removeProduct(String productId) {
        return productMapper.deleteProduct(productId);
    }
    
    @Override
    public List<ProductVO> searchProducts(Map<String, Object> searchParams) {
        return productMapper.selectProductsByCondition(searchParams);
    }
    
    @Override
    public boolean isProductIdExists(String productId) {
        return productMapper.checkProductId(productId) > 0;
    }
    
    private String generateProductId() {
        return "PROD" + String.format("%03d", (int)(System.currentTimeMillis() % 1000));
    }
    
    @Override
    public int approveProduct(String productId, String approver) {
        ProductVO productVO = new ProductVO();
        productVO.setProductId(productId);
        productVO.setStatus(STATUS_APPROVED);  // "ST001" 사용
        productVO.setUpdateUser(approver);
        productVO.setUpdateDate(new Date());
        
        return productMapper.updateProduct(productVO);
    }
    
    @Override
    public int rejectProduct(String productId, String approver, String reason) {
        ProductVO productVO = new ProductVO();
        productVO.setProductId(productId);
        productVO.setStatus(STATUS_HOLD);  // "ST004" 보류 상태 사용
        productVO.setUpdateUser(approver);
        productVO.setUpdateDate(new Date());
        
        if (reason != null && !reason.trim().isEmpty()) {
            productVO.setNote("승인 거부 사유: " + reason);
        }
        
        return productMapper.updateProduct(productVO);
    }
}