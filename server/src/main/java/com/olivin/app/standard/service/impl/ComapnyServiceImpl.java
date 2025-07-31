// package com.olivin.app.standard.service.impl;

// import java.util.Date;
// import java.util.List;
// import java.util.Map;
// import java.util.HashMap;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.olivin.app.standard.mapper.ProductMapper;
// import com.olivin.app.standard.service.ProductService;
// import com.olivin.app.standard.service.ProductVO;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class ComapnyServiceImpl implements CompanyService {
    
//     private final ComapnyMapper companyMapper;
    
//     // 상태 코드 상수 정의 (공통코드 테이블 기준)
//     private static final String STATUS_APPROVED = "040001";  // 정상 (승인완료)
//     private static final String STATUS_PENDING = "040002";   // 대기 (승인대기)
//     private static final String STATUS_REJECTED = "040003";   // 반려 (승인반려)

//     @Override
//     public List<ProductVO> getAllProducts() {
//         return productMapper.selectAllList();
//     }
    
//     @Override
//     public List<ProductVO> getApprovedProducts() {
//         ProductVO searchVO = new ProductVO();
//         searchVO.setStatus(STATUS_APPROVED);  // "040001" 사용
//         return productMapper.selectProductList(searchVO);
//     }
    
//     @Override
//     public List<ProductVO> getProductList(ProductVO productVO) {
//         return productMapper.selectProductList(productVO);
//     }
    
//     @Override
//     public ProductVO getProduct(String productId) {
//         return productMapper.selectProduct(productId);
//     }
    
//     @Override
//     public int saveProduct(ProductVO productVO) {
//         if (productVO.getProductId() == null || productVO.getProductId().isEmpty()) {
//             String newProductId = generateProductId();
//             productVO.setProductId(newProductId);
//         }
        
//         if (productVO.getStatus() == null || productVO.getStatus().isEmpty()) {
//             productVO.setStatus(STATUS_PENDING);  // "040002" 사용
//         }
        
//         productVO.setRegDate(new Date());
        
//         if (productVO.getRegUser() == null || productVO.getRegUser().isEmpty()) {
//             productVO.setRegUser("SYSTEM");
//         }
        
//         return productMapper.insertProduct(productVO);
//     }
    
//     @Override
//     public int modifyProduct(ProductVO productVO) {
//         productVO.setUpdateDate(new Date());
//         return productMapper.updateProduct(productVO);
//     }
    
//     @Override
//     public int removeProduct(String productId) {
//         return productMapper.deleteProduct(productId);
//     }
    
//     @Override
//     public List<ProductVO> searchProducts(Map<String, Object> searchParams) {
//         return productMapper.selectProductsByCondition(searchParams);
//     }
    
//     @Override
//     public boolean isProductIdExists(String productId) {
//         return productMapper.checkProductId(productId) > 0;
//     }
    
//     // ✅ 제품 ID 자동생성 메서드 구현
//     @Override
//     public String getNextProductId(String categoryMain) {
//         // 카테고리별 범위 맵핑 (명세서 기준)
//         Map<String, Map<String, Object>> categoryRanges = new HashMap<>();
//         categoryRanges.put("0001", Map.of("start", 100001, "end", 199999, "prefix", "PRD1")); // 스킨케어
//         categoryRanges.put("0002", Map.of("start", 200001, "end", 299999, "prefix", "PRD2")); // 메이크업
//         categoryRanges.put("0003", Map.of("start", 300001, "end", 399999, "prefix", "PRD3")); // 클렌징
//         categoryRanges.put("0004", Map.of("start", 400001, "end", 499999, "prefix", "PRD4")); // 헤어케어
//         categoryRanges.put("0005", Map.of("start", 500001, "end", 599999, "prefix", "PRD5")); // 구강용품
//         categoryRanges.put("0006", Map.of("start", 600001, "end", 699999, "prefix", "PRD6")); // 선케어
//         categoryRanges.put("0007", Map.of("start", 700001, "end", 799999, "prefix", "PRD7")); // 뷰티소품
//         categoryRanges.put("0008", Map.of("start", 800001, "end", 899999, "prefix", "PRD8")); // 건강/기능 식품
//         categoryRanges.put("0009", Map.of("start", 900001, "end", 999999, "prefix", "PRD9")); // 푸드
        
//         Map<String, Object> range = categoryRanges.get(categoryMain);
//         if (range == null) {
//             return null; // 잘못된 카테고리
//         }
        
//         String prefix = (String) range.get("prefix");
//         Integer startNum = (Integer) range.get("start");
        
//         try {
//             // 해당 카테고리의 마지막 제품 ID 조회
//             String lastProductId = productMapper.selectLastProductIdByCategory(prefix);
            
//             String nextSequence;
//             if (lastProductId != null && !lastProductId.isEmpty()) {
//                 // 마지막 ID에서 숫자 부분 추출 후 +1
//                 String numberPart = lastProductId.substring(4); // "PRD1" 제거
//                 int lastNumber = Integer.parseInt(numberPart);
//                 int nextNumber = lastNumber + 1;
                
//                 // 범위 체크
//                 Integer endNum = (Integer) range.get("end");
//                 if (nextNumber > endNum) {
//                     throw new RuntimeException("해당 카테고리의 제품 ID가 범위를 초과했습니다.");
//                 }
                
//                 nextSequence = String.format("%05d", nextNumber % 100000); // 5자리로 포맷
//             } else {
//                 // 첫 번째 제품인 경우
//                 nextSequence = String.format("%05d", startNum % 100000); // 5자리로 포맷
//             }
            
//             return prefix + nextSequence;
            
//         } catch (Exception e) {
//             System.err.println("제품 ID 생성 중 오류: " + e.getMessage());
//             e.printStackTrace();
//             // 기본값 반환
//             return prefix + "00001";
//         }
//     }
    
//     private String generateProductId() {
//         return "PROD" + String.format("%03d", (int)(System.currentTimeMillis() % 1000));
//     }
    
//     @Override
//     public int approveProduct(String productId, String approver) {
//         System.out.println("=== 승인 처리 시작 ===");
//         System.out.println("ProductId: " + productId);
//         System.out.println("Approver: " + approver);
        
//         try {
//             // ✅ 기존 제품 정보 조회
//             ProductVO existingProduct = productMapper.selectProduct(productId);
//             if (existingProduct == null) {
//                 System.err.println("❌ 제품을 찾을 수 없습니다: " + productId);
//                 return 0;
//             }
            
//             System.out.println("기존 제품 상태: " + existingProduct.getStatus());
            
//             // ✅ 승인 대기 상태인지 확인
//             if (!"040002".equals(existingProduct.getStatus())) {
//                 System.err.println("❌ 승인 대기 상태가 아닙니다. 현재 상태: " + existingProduct.getStatus());
//                 return 0;
//             }
            
//             int result = 0;
            
//             try {
//                 // ✅ 1차 시도: 가장 안전한 간단 승인 쿼리 사용
//                 Map<String, Object> params = new HashMap<>();
//                 params.put("productId", productId);
//                 params.put("status", STATUS_APPROVED);  // "040001"
//                 params.put("updateUser", approver);
                
//                 result = productMapper.updateProductSimpleApproval(params);
//                 System.out.println("간단 승인 업데이트 결과: " + result);
                
//             } catch (Exception e) {
//                 System.out.println("간단 승인 업데이트 실패, 상태 전용 업데이트로 대체: " + e.getMessage());
                
//                 try {
//                     // ✅ 2차 시도: 상태 전용 업데이트
//                     result = productMapper.updateProductStatus(productId, STATUS_APPROVED, approver);
//                     System.out.println("상태 전용 업데이트 결과: " + result);
                    
//                 } catch (Exception e2) {
//                     System.out.println("상태 전용 업데이트도 실패, 안전한 전체 업데이트로 대체: " + e2.getMessage());
                    
//                     // ✅ 3차 시도: 기존 데이터를 보존하면서 상태만 변경
//                     existingProduct.setStatus(STATUS_APPROVED);  // "040001"
//                     existingProduct.setUpdateUser(approver);
//                     existingProduct.setUpdateDate(new Date());
                    
//                     // ✅ null 값 필드들을 기존 값으로 유지
//                     result = productMapper.updateProduct(existingProduct);
//                     System.out.println("안전한 전체 업데이트 결과: " + result);
//                 }
//             }
            
//             // ✅ 업데이트 후 상태 확인
//             try {
//                 String updatedStatus = productMapper.selectProductStatus(productId);
//                 System.out.println("업데이트 후 상태: " + updatedStatus);
                
//                 // ✅ 상태가 실제로 변경되었는지 확인
//                 if (STATUS_APPROVED.equals(updatedStatus)) {
//                     System.out.println("✅ 승인 처리 성공!");
//                 } else {
//                     System.err.println("❌ 상태 변경 실패. 현재 상태: " + updatedStatus);
//                     return 0;
//                 }
//             } catch (Exception e) {
//                 // selectProductStatus 메서드가 없어도 일단 진행
//                 System.out.println("상태 확인 실패, 일반 조회로 대체: " + e.getMessage());
//                 ProductVO updatedProduct = productMapper.selectProduct(productId);
//                 if (updatedProduct != null && STATUS_APPROVED.equals(updatedProduct.getStatus())) {
//                     System.out.println("✅ 승인 처리 성공!");
//                 } else {
//                     System.err.println("❌ 상태 변경 실패");
//                     return 0;
//                 }
//             }
            
//             return result;
            
//         } catch (Exception e) {
//             System.err.println("❌ 승인 처리 중 예외 발생: " + e.getMessage());
//             e.printStackTrace();
//             throw e;
//         }
//     }
    
//     @Override
//     public int rejectProduct(String productId, String approver, String reason) {
//         System.out.println("=== 반려 처리 시작 ===");
//         System.out.println("ProductId: " + productId);
//         System.out.println("Approver: " + approver);
//         System.out.println("Reason: " + reason);
        
//         try {
//             // ✅ 기존 제품 정보 조회
//             ProductVO existingProduct = productMapper.selectProduct(productId);
//             if (existingProduct == null) {
//                 System.err.println("❌ 제품을 찾을 수 없습니다: " + productId);
//                 return 0;
//             }
            
//             System.out.println("기존 제품 상태: " + existingProduct.getStatus());
            
//             // ✅ 승인 대기 상태인지 확인
//             if (!"040002".equals(existingProduct.getStatus())) {
//                 System.err.println("❌ 승인 대기 상태가 아닙니다. 현재 상태: " + existingProduct.getStatus());
//                 return 0;
//             }
            
//             int result = 0;
            
//             try {
//                 // ✅ 1차 시도: 새로운 반려 전용 쿼리 사용
//                 Map<String, Object> params = new HashMap<>();
//                 params.put("productId", productId);
//                 params.put("status", STATUS_REJECTED);  // "040003"
//                 params.put("updateUser", approver);
                
//                 // 반료 사유 추가
//                 if (reason != null && !reason.trim().isEmpty()) {
//                     params.put("note", "승인 거부 사유: " + reason);
//                 }
                
//                 result = productMapper.updateProductApprovalStatus(params);
//                 System.out.println("반려 전용 업데이트 결과: " + result);
                
//             } catch (Exception e) {
//                 System.out.println("반려 전용 업데이트 실패, 안전한 전체 업데이트로 대체: " + e.getMessage());
                
//                 // ✅ 2차 시도: 기존 데이터를 보존하면서 상태와 사유 변경
//                 existingProduct.setStatus(STATUS_REJECTED);  // "040003"
//                 existingProduct.setUpdateUser(approver);
//                 existingProduct.setUpdateDate(new Date());
                
//                 // ✅ 반려 사유 추가
//                 if (reason != null && !reason.trim().isEmpty()) {
//                     String existingNote = existingProduct.getNote();
//                     String newNote = "승인 거부 사유: " + reason;
//                     if (existingNote != null && !existingNote.trim().isEmpty()) {
//                         existingProduct.setNote(existingNote + "\n" + newNote);
//                     } else {
//                         existingProduct.setNote(newNote);
//                     }
//                 }
                
//                 result = productMapper.updateProduct(existingProduct);
//                 System.out.println("안전한 전체 업데이트 결과: " + result);
//             }
            
//             // ✅ 업데이트 후 상태 확인
//             try {
//                 String updatedStatus = productMapper.selectProductStatus(productId);
//                 System.out.println("업데이트 후 상태: " + updatedStatus);
                
//                 // ✅ 상태가 실제로 변경되었는지 확인
//                 if (STATUS_REJECTED.equals(updatedStatus)) {
//                     System.out.println("✅ 반려 처리 성공!");
//                 } else {
//                     System.err.println("❌ 상태 변경 실패. 현재 상태: " + updatedStatus);
//                     return 0;
//                 }
//             } catch (Exception e) {
//                 // selectProductStatus 메서드가 없어도 일단 진행
//                 System.out.println("상태 확인 실패, 일반 조회로 대체: " + e.getMessage());
//                 ProductVO updatedProduct = productMapper.selectProduct(productId);
//                 if (updatedProduct != null && STATUS_REJECTED.equals(updatedProduct.getStatus())) {
//                     System.out.println("✅ 반려 처리 성공!");
//                 } else {
//                     System.err.println("❌ 상태 변경 실패");
//                     return 0;
//                 }
//             }
            
//             return result;
            
//         } catch (Exception e) {
//             System.err.println("❌ 반려 처리 중 예외 발생: " + e.getMessage());
//             e.printStackTrace();
//             throw e;
//         }
//     }
// }