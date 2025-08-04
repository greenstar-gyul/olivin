package com.olivin.app.standard.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.standard.mapper.ProductMapper;
import com.olivin.app.standard.service.ProductService;
import com.olivin.app.standard.service.ProductVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    
    // 상태 코드 상수 정의 (6자리 코드 - 데이터베이스 기준)
    private static final String STATUS_APPROVED = "040001";  // 완료 (승인완료)
    private static final String STATUS_PENDING = "040002";   // 대기 (승인대기)
    private static final String STATUS_REJECTED = "040003";  // 반려 (승인반려)

    /**
     * 모든 제품 조회
     */
    @Override
    public List<ProductVO> getAllProducts() {
        return productMapper.selectAllList();
    }
    
    /**
     * 승인된 제품만 조회
     */
    @Override
    public List<ProductVO> getApprovedProducts() {
        ProductVO searchVO = new ProductVO();
        searchVO.setStatus(STATUS_APPROVED);
        return productMapper.selectProductList(searchVO);
    }
    
    /**
     * 조건에 따른 제품 목록 조회
     */
    @Override
    public List<ProductVO> getProductList(ProductVO productVO) {
        return productMapper.selectProductList(productVO);
    }
    
    /**
     * 제품 ID로 단일 제품 조회
     */
    @Override
    public ProductVO getProduct(String productId) {
        return productMapper.selectProduct(productId);
    }
    
    /**
     * 제품 저장 (신규 등록)
     */
    @Override
    public int saveProduct(ProductVO productVO) {
        // 제품 ID가 없으면 자동 생성
        if (productVO.getProductId() == null || productVO.getProductId().isEmpty()) {
            String newProductId = generateProductId();
            productVO.setProductId(newProductId);
        }
        
        // 상태가 없으면 승인 대기로 설정
        if (productVO.getStatus() == null || productVO.getStatus().isEmpty()) {
            productVO.setStatus(STATUS_PENDING);
        }
        
        // 등록일 설정
        productVO.setRegDate(new Date());
        
        // 등록자가 없으면 기본값 설정
        if (productVO.getRegUser() == null || productVO.getRegUser().isEmpty()) {
            productVO.setRegUser("SYSTEM");
        }
        
        return productMapper.insertProduct(productVO);
    }
    
    /**
     * 제품 정보 수정
     */
    @Override
    public int modifyProduct(ProductVO productVO) {
        productVO.setUpdateDate(new Date());
        return productMapper.updateProduct(productVO);
    }
    
    /**
     * 제품 삭제
     */
    @Override
    public int removeProduct(String productId) {
        return productMapper.deleteProduct(productId);
    }
    
    /**
     * 검색 조건에 따른 제품 조회
     */
    @Override
    public List<ProductVO> searchProducts(Map<String, Object> searchParams) {
        return productMapper.selectProductsByCondition(searchParams);
    }
    
    /**
     * 제품 ID 존재 여부 확인
     */
    @Override
    public boolean isProductIdExists(String productId) {
        return productMapper.checkProductId(productId) > 0;
    }
    
    /**
     * 카테고리별 다음 제품 ID 생성 (5자리 숫자 패턴)
     * 예: PRD100001, PRD200001, PRD300001...
     */
    @Override
    public String getNextProductId(String categoryMain) {
        // 6자리 카테고리 코드별 접두어 매핑
        Map<String, String> categoryPrefixMap = new HashMap<>();
        categoryPrefixMap.put("110001", "PRD1"); // 스킨케어 -> PRD1xxxxx
        categoryPrefixMap.put("110002", "PRD2"); // 메이크업 -> PRD2xxxxx  
        categoryPrefixMap.put("110003", "PRD3"); // 클렌징 -> PRD3xxxxx
        categoryPrefixMap.put("110004", "PRD4"); // 헤어케어 -> PRD4xxxxx
        categoryPrefixMap.put("110005", "PRD5"); // 구강용품 -> PRD5xxxxx
        categoryPrefixMap.put("110006", "PRD6"); // 선케어 -> PRD6xxxxx
        categoryPrefixMap.put("110007", "PRD7"); // 뷰티소품 -> PRD7xxxxx
        categoryPrefixMap.put("110008", "PRD8"); // 건강/기능 식품 -> PRD8xxxxx
        categoryPrefixMap.put("110009", "PRD9"); // 푸드 -> PRD9xxxxx
        
        String prefix = categoryPrefixMap.get(categoryMain);
        if (prefix == null) {
            return null;
        }
        
        try {
            // 해당 카테고리의 마지막 제품 ID 조회
            String lastProductId = productMapper.selectLastProductIdByCategory(prefix);
            
            String nextProductId;
            
            if (lastProductId != null && !lastProductId.isEmpty()) {
                // 마지막 ID에서 숫자 부분 추출 (5자리)
                String numberPart = lastProductId.substring(prefix.length());
                
                try {
                    int lastNumber = Integer.parseInt(numberPart);
                    int nextNumber = lastNumber + 1;
                    
                    // 5자리로 포맷팅 (예: 00001, 00002, 00003...)
                    String formattedNumber = String.format("%05d", nextNumber);
                    nextProductId = prefix + formattedNumber;
                    
                    // 생성된 ID가 이미 존재하는지 재확인
                    boolean exists = productMapper.checkProductId(nextProductId) > 0;
                    
                    if (exists) {
                        // 중복이면 계속 증가시켜서 찾기
                        int attempts = 0;
                        while (exists && attempts < 99999) { // 5자리 최대값까지
                            nextNumber++;
                            if (nextNumber > 99999) {
                                return null; // 5자리 초과시 null 반환
                            }
                            formattedNumber = String.format("%05d", nextNumber);
                            nextProductId = prefix + formattedNumber;
                            exists = productMapper.checkProductId(nextProductId) > 0;
                            attempts++;
                        }
                        
                        if (exists) {
                            return null;
                        }
                    }
                    
                } catch (NumberFormatException e) {
                    // 파싱 오류 시 해당 카테고리의 첫 번째 번호로 설정
                    nextProductId = prefix + "00001"; // 5자리: 00001
                }
                
            } else {
                // 해당 카테고리의 첫 번째 제품인 경우
                nextProductId = prefix + "00001"; // 5자리: 00001
            }
            
            return nextProductId;
            
        } catch (Exception e) {
            // 오류 발생 시 기본값 반환
            return prefix + "00001"; // 5자리: 00001
        }
    }
    
    /**
     * 기본 제품 ID 생성 메서드 (fallback용)
     */
    private String generateProductId() {
        return "PROD" + String.format("%05d", (int)(System.currentTimeMillis() % 100000));
    }
    
    /**
     * 제품 승인 처리
     */
    @Override
    public int approveProduct(String productId, String approver) {
        try {
            // 기존 제품 정보 조회
            ProductVO existingProduct = productMapper.selectProduct(productId);
            if (existingProduct == null) {
                return 0;
            }
            
            // 승인 대기 상태인지 확인 (6자리 코드)
            if (!STATUS_PENDING.equals(existingProduct.getStatus())) {
                return 0;
            }
            
            int result = 0;
            
            try {
                // 1차 시도: 간단 승인 쿼리 사용
                Map<String, Object> params = new HashMap<>();
                params.put("productId", productId);
                params.put("status", STATUS_APPROVED);  // "040001"
                params.put("updateUser", approver);
                
                result = productMapper.updateProductSimpleApproval(params);
                
            } catch (Exception e) {
                try {
                    // 2차 시도: 상태 전용 업데이트
                    result = productMapper.updateProductStatus(productId, STATUS_APPROVED, approver);
                    
                } catch (Exception e2) {
                    // 3차 시도: 기존 데이터를 보존하면서 상태만 변경
                    existingProduct.setStatus(STATUS_APPROVED);  // "040001"
                    existingProduct.setUpdateUser(approver);
                    existingProduct.setUpdateDate(new Date());
                    
                    result = productMapper.updateProduct(existingProduct);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * 제품 반려 처리
     */
    @Override
    public int rejectProduct(String productId, String approver, String reason) {
        try {
            // 기존 제품 정보 조회
            ProductVO existingProduct = productMapper.selectProduct(productId);
            if (existingProduct == null) {
                return 0;
            }
            
            // 승인 대기 상태인지 확인 (6자리 코드)
            if (!STATUS_PENDING.equals(existingProduct.getStatus())) {
                return 0;
            }
            
            int result = 0;
            
            try {
                // 1차 시도: 반려 전용 쿼리 사용
                Map<String, Object> params = new HashMap<>();
                params.put("productId", productId);
                params.put("status", STATUS_REJECTED);  // "040003"
                params.put("updateUser", approver);
                
                // 반려 사유 추가
                if (reason != null && !reason.trim().isEmpty()) {
                    params.put("note", "승인 거부 사유: " + reason);
                }
                
                result = productMapper.updateProductApprovalStatus(params);
                
            } catch (Exception e) {
                // 2차 시도: 기존 데이터를 보존하면서 상태와 사유 변경
                existingProduct.setStatus(STATUS_REJECTED);  // "040003"
                existingProduct.setUpdateUser(approver);
                existingProduct.setUpdateDate(new Date());
                
                // 반려 사유 추가
                if (reason != null && !reason.trim().isEmpty()) {
                    String existingNote = existingProduct.getNote();
                    String newNote = "승인 거부 사유: " + reason;
                    if (existingNote != null && !existingNote.trim().isEmpty()) {
                        existingProduct.setNote(existingNote + "\n" + newNote);
                    } else {
                        existingProduct.setNote(newNote);
                    }
                }
                
                result = productMapper.updateProduct(existingProduct);
            }
            
            return result;
            
        } catch (Exception e) {
            throw e;
        }
    }
}