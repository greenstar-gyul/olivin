package com.olivin.app.standard.service.impl;

import java.util.ArrayList;
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
    
    // ✅ 상태 코드 상수 정의 (중단 상태 추가)
    private static final String STATUS_APPROVED = "040001";  // 완료 (승인완료)
    private static final String STATUS_PENDING = "040002";   // 대기 (승인대기)
    private static final String STATUS_REJECTED = "040003";  // 반려 (승인반려)
    private static final String STATUS_STOPPED = "040004";   // 중단 (제품중단) ✅ 새로 추가

    /**
     * 모든 제품 조회 - 직원 이름 조인 포함
     */
    @Override
    public List<ProductVO> getAllProducts() {
        return productMapper.selectAllList();
    }
    
    /**
     * 승인된 제품만 조회 - 직원 이름 조인 포함
     */
    @Override
    public List<ProductVO> getApprovedProducts() {
        return productMapper.selectApprovedProducts();
    }
    
    /**
     * 승인 대기 제품 조회 - 직원 이름 조인 포함
     */
    @Override
    public List<ProductVO> getPendingProducts() {
        return productMapper.selectPendingProducts();
    }
    
    /**
     * 조건에 따른 제품 목록 조회 - 직원 이름 조인 포함
     */
    @Override
    public List<ProductVO> getProductList(ProductVO productVO) {
        return productMapper.selectProductList(productVO);
    }
    
    /**
     * 제품 ID로 단일 제품 조회 - 직원 이름 조인 포함
     */
    @Override
    public ProductVO getProduct(String productId) {
        return productMapper.selectProduct(productId);
    }
    
    /**
     * ✅ 제품 저장 (신규 등록) - 날짜 처리 개선
     */
    @Override
    public int saveProduct(ProductVO productVO) {
        System.out.println("=== saveProduct 시작 ===");
        System.out.println("입력된 ProductVO: " + productVO.toString());
        
        // ✅ 날짜 데이터 처리 개선
        if (productVO.getRegDate() != null) {
            System.out.println("원본 등록일: " + productVO.getRegDate() + " (타입: " + productVO.getRegDate().getClass().getSimpleName() + ")");
            
            // Date 객체가 올바른지 확인
            try {
                long timestamp = productVO.getRegDate().getTime();
                System.out.println("등록일 타임스탬프: " + timestamp);
                
                // 유효하지 않은 날짜인지 확인 (예: 1970년 이전이나 너무 미래)
                if (timestamp < 0 || timestamp > System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)) {
                    System.err.println("⚠️ 의심스러운 등록일, 현재 시간으로 대체");
                    productVO.setRegDate(new Date());
                }
            } catch (Exception e) {
                System.err.println("❌ 등록일 검증 실패, 현재 시간으로 대체: " + e.getMessage());
                productVO.setRegDate(new Date());
            }
        }
        
        // 제품 ID 자동 생성 (카테고리 기반)
        if (productVO.getCategoryMain() != null && !productVO.getCategoryMain().isEmpty()) {
            System.out.println("🔄 제품 ID 자동 생성 시작...");
            String newProductId = getNextProductId(productVO.getCategoryMain());
            
            if (newProductId != null && !newProductId.isEmpty()) {
                productVO.setProductId(newProductId);
                System.out.println("✅ 생성된 제품 ID: " + newProductId);
                
                // 중복 확인
                try {
                    boolean exists = productMapper.checkProductId(newProductId) > 0;
                    if (exists) {
                        System.err.println("❌ 경고: 생성된 제품 ID가 이미 존재합니다: " + newProductId);
                        throw new RuntimeException("제품 ID 생성 중 중복이 발생했습니다. 다시 시도해주세요.");
                    }
                    System.out.println("✅ 중복 확인 완료: " + newProductId);
                } catch (Exception e) {
                    System.err.println("❌ 중복 확인 중 오류: " + e.getMessage());
                    throw new RuntimeException("제품 ID 중복 확인 중 오류가 발생했습니다.", e);
                }
            } else {
                System.err.println("❌ 제품 ID 생성 실패");
                throw new RuntimeException("제품 ID 생성에 실패했습니다. 카테고리를 확인해주세요.");
            }
        } else {
            System.err.println("❌ 카테고리가 없음");
            throw new RuntimeException("카테고리는 필수입니다.");
        }
        
        // 상태가 없으면 승인 대기로 설정
        if (productVO.getStatus() == null || productVO.getStatus().isEmpty()) {
            productVO.setStatus(STATUS_PENDING);
            System.out.println("📝 상태를 승인 대기로 설정: " + STATUS_PENDING);
        }
        
        // 등록일 설정 (사용자가 입력한 값이 없으면 현재 시간)
        if (productVO.getRegDate() == null) {
            productVO.setRegDate(new Date());
            System.out.println("📅 등록일을 현재 시간으로 설정");
        }
        
        // 등록자가 없으면 기본값 설정
        if (productVO.getRegUser() == null || productVO.getRegUser().isEmpty()) {
            productVO.setRegUser("SYSTEM");
            System.out.println("👤 등록자를 SYSTEM으로 설정");
        }
        
        System.out.println("=== 최종 저장 데이터 ===");
        System.out.println("제품ID: " + productVO.getProductId());
        System.out.println("제품명: " + productVO.getProductName());
        System.out.println("카테고리: " + productVO.getCategoryMain());
        System.out.println("등록자: " + productVO.getRegUser());
        System.out.println("등록일: " + productVO.getRegDate());
        System.out.println("상태: " + productVO.getStatus());
        
        try {
            int result = productMapper.insertProduct(productVO);
            System.out.println("💾 데이터베이스 저장 결과: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("❌ 데이터베이스 저장 실패:");
            System.err.println("SQL 상태: " + (e instanceof java.sql.SQLException ? ((java.sql.SQLException) e).getSQLState() : "N/A"));
            System.err.println("오류 코드: " + (e instanceof java.sql.SQLException ? ((java.sql.SQLException) e).getErrorCode() : "N/A"));
            e.printStackTrace();
            throw new RuntimeException("제품 저장 중 데이터베이스 오류가 발생했습니다: " + e.getMessage(), e);
        }
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
     * 검색 조건에 따른 제품 조회 - 기존 코드에 예외 처리만 추가
     */
    @Override
    public List<ProductVO> searchProducts(Map<String, Object> searchParams) {
        try {
            System.out.println("=== ProductService 검색 시작 ===");
            System.out.println("검색 파라미터: " + searchParams);
            
            // 파라미터 유효성 검사
            if (searchParams == null || searchParams.isEmpty()) {
                System.out.println("검색 파라미터가 비어있습니다.");
                return new ArrayList<>();
            }
            
            // 파라미터 로깅
            searchParams.forEach((key, value) -> {
                System.out.println("파라미터 [" + key + "]: " + value + 
                    " (타입: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            });
            
            // MyBatis 매퍼 호출
            List<ProductVO> results = productMapper.selectProductsByCondition(searchParams);
            
            System.out.println("✅ 검색 완료: " + (results != null ? results.size() : 0) + "개 결과");
            
            return results != null ? results : new ArrayList<>();
            
        } catch (Exception e) {
            System.err.println("=== 검색 중 오류 발생 ===");
            System.err.println("오류 타입: " + e.getClass().getSimpleName());
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            // 빈 리스트 반환 (예외를 다시 던지지 않음)
            return new ArrayList<>();
        }
    }
    
    /**
     * 제품 ID 존재 여부 확인
     */
    @Override
    public boolean isProductIdExists(String productId) {
        return productMapper.checkProductId(productId) > 0;
    }
    
    /**
     * 카테고리별 다음 제품 ID 생성 (5자리 숫자 패턴) - 강화된 디버깅 버전
     * 예: PRD100001, PRD200001, PRD300001...
     */
    @Override
    public String getNextProductId(String categoryMain) {
        System.out.println("=== getNextProductId 호출 시작 ===");
        System.out.println("입력 카테고리: " + categoryMain);
        
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
            System.err.println("❌ 지원하지 않는 카테고리: " + categoryMain);
            System.err.println("사용 가능한 카테고리: " + categoryPrefixMap.keySet());
            return null;
        }
        
        System.out.println("✅ 카테고리 매핑 성공: " + categoryMain + " -> " + prefix);
        
        try {
            // 해당 카테고리의 마지막 제품 ID 조회
            System.out.println("💾 데이터베이스에서 마지막 제품 ID 조회 시작...");
            System.out.println("검색할 접두어: " + prefix);
            
            String lastProductId = null;
            try {
                lastProductId = productMapper.selectLastProductIdByCategory(prefix);
                System.out.println("🔍 매퍼 쿼리 실행 결과: " + lastProductId);
            } catch (Exception mapperException) {
                System.err.println("💥 매퍼 쿼리 실행 실패:");
                mapperException.printStackTrace();
                
                // 매퍼 오류 시 폴백: 기본값으로 시작
                System.out.println("🔄 매퍼 오류로 인한 폴백 처리");
                lastProductId = null;
            }
            
            int nextNumber = 1; // 기본값
            
            if (lastProductId != null && !lastProductId.isEmpty()) {
                try {
                    // 마지막 ID에서 숫자 부분 추출 (5자리)
                    System.out.println("🔢 숫자 부분 추출 시작...");
                    System.out.println("전체 ID: " + lastProductId);
                    System.out.println("접두어 길이: " + prefix.length());
                    
                    if (lastProductId.length() <= prefix.length()) {
                        System.err.println("❌ 제품 ID 길이가 접두어보다 짧음");
                        throw new StringIndexOutOfBoundsException("ID 길이 부족");
                    }
                    
                    String numberPart = lastProductId.substring(prefix.length());
                    System.out.println("추출된 숫자 부분: '" + numberPart + "'");
                    
                    if (numberPart.trim().isEmpty()) {
                        System.err.println("❌ 숫자 부분이 비어있음");
                        throw new NumberFormatException("숫자 부분 없음");
                    }
                    
                    int lastNumber = Integer.parseInt(numberPart.trim());
                    nextNumber = lastNumber + 1;
                    
                    System.out.println("✅ 파싱 성공 - 마지막 번호: " + lastNumber + ", 다음 번호: " + nextNumber);
                    
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    System.err.println("❌ 제품 ID 파싱 오류: " + lastProductId);
                    System.err.println("오류 세부사항: " + e.getMessage());
                    e.printStackTrace();
                    nextNumber = 1; // 파싱 실패 시 1부터 시작
                    System.out.println("🔄 파싱 실패로 인해 1부터 시작");
                }
            } else {
                System.out.println("📝 마지막 제품 ID가 없음 - 1부터 시작");
            }
            
            // 중복 방지를 위한 반복 검사
            String nextProductId;
            int attempts = 0;
            int maxAttempts = 100; // 최대 100번 시도
            
            System.out.println("🔄 중복 확인 루프 시작...");
            
            do {
                // 5자리로 포맷팅 (예: 00001, 00002, 00003...)
                String formattedNumber = String.format("%05d", nextNumber);
                nextProductId = prefix + formattedNumber;
                
                System.out.println("시도 " + (attempts + 1) + ": 생성된 제품 ID = " + nextProductId);
                
                // 중복 확인
                boolean exists = false;
                try {
                    exists = productMapper.checkProductId(nextProductId) > 0;
                    System.out.println("중복 확인 결과: " + (exists ? "중복됨" : "사용 가능"));
                } catch (Exception checkException) {
                    System.err.println("💥 중복 확인 중 오류:");
                    checkException.printStackTrace();
                    // 중복 확인 실패 시 안전하게 다음 번호로
                    exists = true;
                }
                
                if (!exists) {
                    // 중복이 아니면 사용 가능
                    System.out.println("✅ 사용 가능한 제품 ID 찾음: " + nextProductId);
                    System.out.println("=== getNextProductId 성공 종료 ===");
                    return nextProductId;
                }
                
                System.out.println("❌ 중복된 제품 ID: " + nextProductId + ", 다음 번호로 시도");
                nextNumber++;
                attempts++;
                
                // 5자리 최대값 초과 체크
                if (nextNumber > 99999) {
                    System.err.println("💥 카테고리 " + prefix + "의 모든 제품 ID가 사용됨 (99999 초과)");
                    System.out.println("=== getNextProductId 실패 종료 ===");
                    return null;
                }
                
            } while (attempts < maxAttempts);
            
            System.err.println("💥 제품 ID 생성 시도 횟수 초과: " + maxAttempts);
            System.out.println("=== getNextProductId 실패 종료 ===");
            return null;
            
        } catch (Exception e) {
            System.err.println("💥 제품 ID 생성 중 예외 발생:");
            System.err.println("예외 타입: " + e.getClass().getSimpleName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            
            // 예외 발생 시 기본값으로 시도
            System.out.println("🔄 예외 복구 시도...");
            String fallbackId = prefix + "00001";
            
            try {
                boolean exists = productMapper.checkProductId(fallbackId) > 0;
                
                if (!exists) {
                    System.out.println("✅ 예외 복구 성공: 기본값 사용 " + fallbackId);
                    System.out.println("=== getNextProductId 복구 성공 종료 ===");
                    return fallbackId;
                } else {
                    System.err.println("❌ 기본값도 중복됨: " + fallbackId);
                }
            } catch (Exception fallbackException) {
                System.err.println("💥 폴백 처리도 실패:");
                fallbackException.printStackTrace();
            }
            
            System.out.println("=== getNextProductId 완전 실패 종료 ===");
            return null;
        }
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
    
    /**
     * ✅ 제품 중단 처리 - 상태를 중단(040004)으로 변경
     */
    @Override
    public int stopProduct(String productId, String updateUser) {
        try {
            // 기존 제품 정보 조회
            ProductVO existingProduct = productMapper.selectProduct(productId);
            if (existingProduct == null) {
                throw new RuntimeException("존재하지 않는 제품입니다: " + productId);
            }
            
            System.out.println("=== 제품 중단 처리 시작 ===");
            System.out.println("제품ID: " + productId);
            System.out.println("현재상태: " + existingProduct.getStatus());
            System.out.println("처리자: " + updateUser);
            
            int result = 0;
            
            try {
                // 1차 시도: 상태 전용 업데이트
                result = productMapper.updateProductStatus(productId, STATUS_STOPPED, updateUser);
                System.out.println("상태 전용 업데이트 결과: " + result);
                
            } catch (Exception e) {
                System.err.println("상태 전용 업데이트 실패, 전체 업데이트 시도: " + e.getMessage());
                
                // 2차 시도: 기존 데이터를 보존하면서 상태만 변경
                existingProduct.setStatus(STATUS_STOPPED);  // 중단 상태 "040004"
                existingProduct.setUpdateUser(updateUser);
                existingProduct.setUpdateDate(new Date());
                
                // 중단 사유를 비고에 추가
                String existingNote = existingProduct.getNote();
                String stopNote = "제품 중단 처리됨 - " + new Date();
                if (existingNote != null && !existingNote.trim().isEmpty()) {
                    existingProduct.setNote(existingNote + "\n" + stopNote);
                } else {
                    existingProduct.setNote(stopNote);
                }
                
                result = productMapper.updateProduct(existingProduct);
                System.out.println("전체 업데이트 결과: " + result);
            }
            
            if (result > 0) {
                System.out.println("✅ 제품 중단 처리 성공");
            } else {
                System.err.println("❌ 제품 중단 처리 실패");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("=== 제품 중단 처리 중 오류 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("제품 중단 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}