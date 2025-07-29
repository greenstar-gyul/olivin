package com.olivin.app.standard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.standard.service.ProductVO;

@Mapper
public interface ProductMapper {
    
    List<ProductVO> selectAllList();
    List<ProductVO> selectProductList(ProductVO productVO);
    ProductVO selectProduct(String productId);
    int insertProduct(ProductVO productVO);
    int updateProduct(ProductVO productVO);
    int deleteProduct(String productId);
    List<ProductVO> selectProductsByCondition(Map<String, Object> params);
    int checkProductId(String productId);
    
    // ✅ 제품 ID 자동생성을 위한 메서드 추가
    String selectLastProductIdByCategory(@Param("prefix") String prefix);
    
    // ✅ 상태 전용 업데이트 메서드들
    int updateProductStatus(@Param("productId") String productId, 
                            @Param("status") String status, 
                            @Param("updateUser") String updateUser);
    
    int updateProductApprovalStatus(Map<String, Object> params);
    
    // ✅ 임시 간단한 승인/반려 메서드 (가장 안전)
    int updateProductSimpleApproval(Map<String, Object> params);
    
    // ✅ 디버깅 및 조회 메서드들
    String selectProductStatus(String productId);
    List<Map<String, Object>> selectProductCountByStatus();
    Map<String, Object> selectProductStatusHistory(String productId);
    
    // ✅ 상태별 제품 조회 메서드들
    List<ProductVO> selectPendingProducts();
    List<ProductVO> selectApprovedProducts();
}