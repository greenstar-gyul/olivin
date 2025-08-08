package com.olivin.app.standard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.standard.service.ProductVO;

@Mapper
public interface ProductMapper {
    
    // 기본 조회 메서드들 - 모두 조인 포함
    List<ProductVO> selectAllList();
    List<ProductVO> selectProductList(ProductVO productVO);
    ProductVO selectProduct(String productId);
    List<ProductVO> selectProductsByCondition(Map<String, Object> params);
    
    // 승인 관련 조회 - 조인 포함
    List<ProductVO> selectPendingProducts();
    List<ProductVO> selectApprovedProducts();
    
    // 데이터 변경 메서드들
    int insertProduct(ProductVO productVO);
    int updateProduct(ProductVO productVO);
    int deleteProduct(String productId);
    int checkProductId(String productId);
    
    // 제품 ID 자동생성을 위한 메서드
    String selectLastProductIdByCategory(@Param("prefix") String prefix);
    
    // 상태 전용 업데이트 메서드들
    int updateProductStatus(@Param("productId") String productId, 
                            @Param("status") String status, 
                            @Param("updateUser") String updateUser);
    
    int updateProductApprovalStatus(Map<String, Object> params);
    int updateProductSimpleApproval(Map<String, Object> params);
    
    // ✅ 제품 중단 전용 업데이트 (선택사항 - 기존 updateProductStatus 사용 가능)
    int updateProductStopStatus(Map<String, Object> params);
    
    // 단순 조회 메서드들
    String selectProductStatus(String productId);
}