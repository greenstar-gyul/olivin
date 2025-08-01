package com.olivin.app.standard.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
     * 카테고리별 다음 제품 ID 생성
     * DB의 실제 마지막 제품 ID를 조회하여 다음 순번 생성
     */
    // ProductServiceImpl.java의 getNextProductId 메서드 수정

    @Override
    public String getNextProductId(String categoryMain) {
        System.out.println("=== 제품 ID 생성 시작 ===");
        System.out.println("요청된 카테고리: " + categoryMain);
        
        // 6자리 카테고리 코드별 접두어 매핑
        Map<String, String> categoryPrefixMap = new HashMap<>();
        categoryPrefixMap.put("110001", "PRD1"); // 스킨케어
        categoryPrefixMap.put("110002", "PRD2"); // 메이크업  
        categoryPrefixMap.put("110003", "PRD3"); // 클렌징
        categoryPrefixMap.put("110004", "PRD4"); // 헤어케어
        categoryPrefixMap.put("110005", "PRD5"); // 구강용품
        categoryPrefixMap.put("110006", "PRD6"); // 선케어
        categoryPrefixMap.put("110007", "PRD7"); // 뷰티소품
        categoryPrefixMap.put("110008", "PRD8"); // 건강/기능 식품
        categoryPrefixMap.put("110009", "PRD9"); // 푸드
        
        String prefix = categoryPrefixMap.get(categoryMain);
        if (prefix == null) {
            System.err.println("❌ 잘못된 카테고리 코드: " + categoryMain);
            return null;
        }
        
        System.out.println("카테고리 매핑 결과 - 접두어: " + prefix);
        
        try {
            // 해당 카테고리의 마지막 제품 ID 조회
            String lastProductId = productMapper.selectLastProductIdByCategory(prefix);
            System.out.println("DB에서 조회된 마지막 제품 ID: " + lastProductId);
            
            String nextProductId;
            
            if (lastProductId != null && !lastProductId.isEmpty()) {
                // 마지막 ID에서 숫자 부분 추출
                // PRD100018 -> 100018 추출 (prefix 길이만큼 자르기)
                String numberPart = lastProductId.substring(prefix.length());
                int lastNumber = Integer.parseInt(numberPart);
                int nextNumber = lastNumber + 1;
                
                System.out.println("숫자 부분: " + numberPart);
                System.out.println("마지막 번호: " + lastNumber + " -> 다음 번호: " + nextNumber);
                
                // ✅ 기존 패턴과 동일한 자리수로 포맷팅
                // PRD100018의 패턴: PRD + 1 + 00018 (5자리)
                // 즉, 100018에서 다음은 100019가 되어야 함
                String formattedNumber = String.format("%06d", nextNumber);
                nextProductId = prefix + formattedNumber;
                
            } else {
                // ✅ 첫 번째 제품인 경우 - 기존 패턴 분석
                // 현재 DB: PRD100001 ~ PRD100018 패턴
                // 따라서 첫 제품은 PRD100001이어야 함
                System.out.println("해당 카테고리의 첫 번째 제품");
                
                // 스킨케어(PRD1)의 경우 100001부터 시작
                String categoryNumber = prefix.substring(3); // "PRD1"에서 "1" 추출
                int startNumber = Integer.parseInt(categoryNumber) * 100000 + 1; // 1 -> 100001
                
                nextProductId = prefix + String.format("%06d", startNumber);
            }
            
            System.out.println("✅ 생성된 제품 ID: " + nextProductId);
            return nextProductId;
            
        } catch (Exception e) {
            System.err.println("❌ 제품 ID 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 기본값 반환
            String categoryNumber = prefix.substring(3);
            int startNumber = Integer.parseInt(categoryNumber) * 100000 + 1;
            String fallbackId = prefix + String.format("%06d", startNumber);
            
            System.out.println("기본값 반환: " + fallbackId);
            return fallbackId;
        }
    }
    
    /**
     * 기본 제품 ID 생성 메서드 (fallback용)
     */
    private String generateProductId() {
        return "PROD" + String.format("%03d", (int)(System.currentTimeMillis() % 1000));
    }
    
    /**
     * 제품 승인 처리
     */
    @Override
    public int approveProduct(String productId, String approver) {
        System.out.println("=== 승인 처리 시작 ===");
        System.out.println("ProductId: " + productId);
        System.out.println("Approver: " + approver);
        
        try {
            // 기존 제품 정보 조회
            ProductVO existingProduct = productMapper.selectProduct(productId);
            if (existingProduct == null) {
                System.err.println("제품을 찾을 수 없습니다: " + productId);
                return 0;
            }
            
            System.out.println("기존 제품 상태: " + existingProduct.getStatus());
            
            // 승인 대기 상태인지 확인 (6자리 코드)
            if (!STATUS_PENDING.equals(existingProduct.getStatus())) {
                System.err.println("승인 대기 상태가 아닙니다. 현재 상태: " + existingProduct.getStatus());
                return 0;
            }
            
            int result = 0;
            
            try {
                // 1차 시도: 가장 안전한 간단 승인 쿼리 사용
                Map<String, Object> params = new HashMap<>();
                params.put("productId", productId);
                params.put("status", STATUS_APPROVED);  // "040001"
                params.put("updateUser", approver);
                
                result = productMapper.updateProductSimpleApproval(params);
                System.out.println("간단 승인 업데이트 결과: " + result);
                
            } catch (Exception e) {
                System.out.println("간단 승인 업데이트 실패, 상태 전용 업데이트로 대체: " + e.getMessage());
                
                try {
                    // 2차 시도: 상태 전용 업데이트
                    result = productMapper.updateProductStatus(productId, STATUS_APPROVED, approver);
                    System.out.println("상태 전용 업데이트 결과: " + result);
                    
                } catch (Exception e2) {
                    System.out.println("상태 전용 업데이트도 실패, 안전한 전체 업데이트로 대체: " + e2.getMessage());
                    
                    // 3차 시도: 기존 데이터를 보존하면서 상태만 변경
                    existingProduct.setStatus(STATUS_APPROVED);  // "040001"
                    existingProduct.setUpdateUser(approver);
                    existingProduct.setUpdateDate(new Date());
                    
                    result = productMapper.updateProduct(existingProduct);
                    System.out.println("안전한 전체 업데이트 결과: " + result);
                }
            }
            
            // 업데이트 후 상태 확인
            try {
                String updatedStatus = productMapper.selectProductStatus(productId);
                System.out.println("업데이트 후 상태: " + updatedStatus);
                
                // 상태가 실제로 변경되었는지 확인
                if (STATUS_APPROVED.equals(updatedStatus)) {
                    System.out.println("승인 처리 성공!");
                } else {
                    System.err.println("상태 변경 실패. 현재 상태: " + updatedStatus);
                    return 0;
                }
            } catch (Exception e) {
                // selectProductStatus 메서드가 없어도 일반 조회로 대체
                System.out.println("상태 확인 실패, 일반 조회로 대체: " + e.getMessage());
                ProductVO updatedProduct = productMapper.selectProduct(productId);
                if (updatedProduct != null && STATUS_APPROVED.equals(updatedProduct.getStatus())) {
                    System.out.println("승인 처리 성공!");
                } else {
                    System.err.println("상태 변경 실패");
                    return 0;
                }
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("승인 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 제품 반려 처리
     */
    @Override
    public int rejectProduct(String productId, String approver, String reason) {
        System.out.println("=== 반려 처리 시작 ===");
        System.out.println("ProductId: " + productId);
        System.out.println("Approver: " + approver);
        System.out.println("Reason: " + reason);
        
        try {
            // 기존 제품 정보 조회
            ProductVO existingProduct = productMapper.selectProduct(productId);
            if (existingProduct == null) {
                System.err.println("제품을 찾을 수 없습니다: " + productId);
                return 0;
            }
            
            System.out.println("기존 제품 상태: " + existingProduct.getStatus());
            
            // 승인 대기 상태인지 확인 (6자리 코드)
            if (!STATUS_PENDING.equals(existingProduct.getStatus())) {
                System.err.println("승인 대기 상태가 아닙니다. 현재 상태: " + existingProduct.getStatus());
                return 0;
            }
            
            int result = 0;
            
            try {
                // 1차 시도: 새로운 반려 전용 쿼리 사용
                Map<String, Object> params = new HashMap<>();
                params.put("productId", productId);
                params.put("status", STATUS_REJECTED);  // "040003"
                params.put("updateUser", approver);
                
                // 반려 사유 추가
                if (reason != null && !reason.trim().isEmpty()) {
                    params.put("note", "승인 거부 사유: " + reason);
                }
                
                result = productMapper.updateProductApprovalStatus(params);
                System.out.println("반려 전용 업데이트 결과: " + result);
                
            } catch (Exception e) {
                System.out.println("반려 전용 업데이트 실패, 안전한 전체 업데이트로 대체: " + e.getMessage());
                
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
                System.out.println("안전한 전체 업데이트 결과: " + result);
            }
            
            // 업데이트 후 상태 확인
            try {
                String updatedStatus = productMapper.selectProductStatus(productId);
                System.out.println("업데이트 후 상태: " + updatedStatus);
                
                // 상태가 실제로 변경되었는지 확인
                if (STATUS_REJECTED.equals(updatedStatus)) {
                    System.out.println("반려 처리 성공!");
                } else {
                    System.err.println("상태 변경 실패. 현재 상태: " + updatedStatus);
                    return 0;
                }
            } catch (Exception e) {
                // selectProductStatus 메서드가 없어도 일반 조회로 대체
                System.out.println("상태 확인 실패, 일반 조회로 대체: " + e.getMessage());
                ProductVO updatedProduct = productMapper.selectProduct(productId);
                if (updatedProduct != null && STATUS_REJECTED.equals(updatedProduct.getStatus())) {
                    System.out.println("반려 처리 성공!");
                } else {
                    System.err.println("상태 변경 실패");
                    return 0;
                }
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("반려 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}