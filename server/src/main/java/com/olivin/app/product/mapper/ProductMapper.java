package com.olivin.app.product.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.olivin.app.product.service.ProductVO;

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
}