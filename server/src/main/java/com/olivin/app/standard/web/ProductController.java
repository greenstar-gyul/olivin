package com.olivin.app.standard.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.olivin.app.standard.service.ProductService;
import com.olivin.app.standard.service.ProductVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")  
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    // application.yml에서 설정할 수 있도록 변경
    @Value("${app.upload.dir:uploads/products/}")
    private String uploadDir;
    
    // 업로드 디렉토리를 프로젝트 루트 하위로 설정
    private String getUploadDirectory() {
        // 현재 작업 디렉토리 기준으로 uploads 폴더 생성
        String currentDir = System.getProperty("user.dir");
        return currentDir + File.separator + "uploads" + File.separator + "products" + File.separator;
    }

    /**
     * ✅ 서버 연결 확인용 핑 API (다른 컨트롤러와 일관성 맞춤)
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "제품 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 모든 제품 조회 - 다른 컨트롤러와 동일한 응답 구조
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ProductVO> products = productService.getAllProducts();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", products);
            
            // 로깅: 조인된 직원 이름 확인
            if (!products.isEmpty()) {
                System.out.println("첫 번째 제품의 직원 정보:");
                ProductVO firstProduct = products.get(0);
                System.out.println("REG_USER: " + firstProduct.getRegUser());
                System.out.println("REG_USER_NAME: " + firstProduct.getRegUserName());
                System.out.println("UPDATE_USER: " + firstProduct.getUpdateUser());
                System.out.println("UPDATE_USER_NAME: " + firstProduct.getUpdateUserName());
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
 * ✅ 승인된 제품만 조회 - 상세 로깅 및 디버깅 강화
    */
    @GetMapping("/approved")
    public ResponseEntity<Map<String, Object>> getApprovedProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 승인된 제품 조회 API 호출 ===");
            
            List<ProductVO> products = productService.getApprovedProducts();
            
            System.out.println("🔍 조회된 제품 수: " + (products != null ? products.size() : 0));
            
            if (products != null && !products.isEmpty()) {
                // 상세 로깅: 각 제품의 상태 확인
                System.out.println("📋 승인된 제품 목록:");
                for (int i = 0; i < Math.min(products.size(), 10); i++) { // 최대 10개만 로깅
                    ProductVO product = products.get(i);
                    System.out.println(String.format("  %d. %s (ID: %s, 상태: %s, 등록자: %s)", 
                        i + 1, 
                        product.getProductName(), 
                        product.getProductId(), 
                        product.getStatus(),
                        product.getRegUserName()));
                }
                if (products.size() > 10) {
                    System.out.println("  ... 및 " + (products.size() - 10) + "개 더");
                }
            } else {
                System.out.println("⚠️ 승인된 제품이 없습니다.");
                
                // 디버깅: 전체 제품 수 확인
                try {
                    List<ProductVO> allProducts = productService.getAllProducts();
                    System.out.println("🔍 전체 제품 수: " + (allProducts != null ? allProducts.size() : 0));
                    
                    if (allProducts != null && !allProducts.isEmpty()) {
                        // 상태별 통계
                        Map<String, Integer> statusCount = new HashMap<>();
                        for (ProductVO product : allProducts) {
                            String status = product.getStatus();
                            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
                        }
                        
                        System.out.println("📊 제품 상태별 통계:");
                        statusCount.forEach((status, count) -> {
                            String statusName = switch (status) {
                                case "040001" -> "승인완료";
                                case "040002" -> "승인대기";
                                case "040003" -> "승인반려";
                                case "040004" -> "제품중단";
                                default -> "알수없음";
                            };
                            System.out.println("  " + status + " (" + statusName + "): " + count + "개");
                        });
                    }
                } catch (Exception debugException) {
                    System.err.println("🔍 디버깅 중 오류: " + debugException.getMessage());
                }
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", products != null ? products : new ArrayList<>());
            
            // 로깅: 조인된 직원 이름 확인
            if (products != null && !products.isEmpty()) {
                ProductVO firstProduct = products.get(0);
                System.out.println("👥 첫 번째 제품의 직원 정보:");
                System.out.println("  등록자ID: " + firstProduct.getRegUser());
                System.out.println("  등록자명: " + firstProduct.getRegUserName());
                System.out.println("  수정자ID: " + firstProduct.getUpdateUser());
                System.out.println("  수정자명: " + firstProduct.getUpdateUserName());
            }
            
        } catch (Exception e) {
            System.err.println("❌ 승인된 제품 목록 조회 실패:");
            System.err.println("  오류 타입: " + e.getClass().getSimpleName());
            System.err.println("  오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("result_code", "FAIL");
            result.put("message", "승인된 제품 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        System.out.println("=== 승인된 제품 조회 API 응답 완료 ===");
        return ResponseEntity.ok(result);
    }
    /**
     * ✅ 승인 대기 제품 조회 - 다른 컨트롤러와 동일한 응답 구조
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ProductVO> products = productService.getPendingProducts();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", products);
            
            // 로깅: 승인 대기 제품의 직원 정보 확인
            System.out.println("승인 대기 제품 수: " + products.size());
            products.forEach(product -> {
                System.out.println("제품ID: " + product.getProductId() + 
                                 ", 등록자ID: " + product.getRegUser() + 
                                 ", 등록자명: " + product.getRegUserName());
            });
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "승인 대기 제품 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 목록 조회 (POST 방식) - 다른 컨트롤러와 동일한 응답 구조
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> getProductList(@RequestBody ProductVO productVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ProductVO> products = productService.getProductList(productVO);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", products);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 검색 (GET 방식, 파라미터 기반) - 다른 컨트롤러와 동일한 응답 구조
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 검색 파라미터 처리 시작 ===");
            System.out.println("원본 파라미터: " + params);
            
            Map<String, Object> processedParams = new HashMap<>(params);
            
            // ✅ 날짜 파라미터 처리 강화 (다양한 형식 지원)
            String[] dateParams = {"regDateFrom", "regDateTo"};
            
            for (String dateParamName : dateParams) {
                if (params.containsKey(dateParamName) && params.get(dateParamName) != null) {
                    String dateValue = params.get(dateParamName).toString().trim();
                    
                    if (!dateValue.isEmpty()) {
                        try {
                            // 다양한 날짜 형식 처리
                            java.sql.Date sqlDate = null;
                            
                            // 1. YYYY-MM-DD 형식 (가장 일반적)
                            if (dateValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                                sqlDate = java.sql.Date.valueOf(dateValue);
                                
                            // 2. ISO 8601 형식 (2024-01-01T00:00:00.000Z)
                            } else if (dateValue.contains("T")) {
                                Instant instant = Instant.parse(dateValue);
                                sqlDate = new java.sql.Date(instant.toEpochMilli());
                                
                            // 3. 타임스탬프 (밀리초)
                            } else if (dateValue.matches("\\d{13}")) {
                                long timestamp = Long.parseLong(dateValue);
                                sqlDate = new java.sql.Date(timestamp);
                                
                            // 4. 기타 형식은 Date 파싱 시도
                            } else {
                                Date parsedDate = new Date(dateValue);
                                sqlDate = new java.sql.Date(parsedDate.getTime());
                            }
                            
                            if (sqlDate != null) {
                                processedParams.put(dateParamName, sqlDate);
                                System.out.println("✅ " + dateParamName + " 변환 성공: " + dateValue + " -> " + sqlDate);
                            }
                            
                        } catch (Exception e) {
                            System.err.println("❌ " + dateParamName + " 변환 실패: " + dateValue + ", 오류: " + e.getMessage());
                            // 변환 실패 시 해당 파라미터 제거
                            processedParams.remove(dateParamName);
                        }
                    }
                }
            }
            
            System.out.println("처리된 파라미터: " + processedParams);
            
            List<ProductVO> products = productService.searchProducts(processedParams);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", products);
            
            System.out.println("✅ 검색 완료: " + products.size() + "개 결과");
            
        } catch (Exception e) {
            System.err.println("=== 검색 처리 중 오류 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("result_code", "FAIL");
            result.put("message", "검색 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 특정 제품 조회 - 다른 컨트롤러와 동일한 응답 구조
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ProductVO product = productService.getProduct(productId);
            
            if (product != null) {
                result.put("result_code", "SUCCESS");
                result.put("message", "성공");
                result.put("data", product);
                
                // 로깅: 조회된 제품의 직원 정보 확인
                System.out.println("조회된 제품: " + productId);
                System.out.println("등록자ID: " + product.getRegUser() + ", 등록자명: " + product.getRegUserName());
                System.out.println("수정자ID: " + product.getUpdateUser() + ", 수정자명: " + product.getUpdateUserName());
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품을 찾을 수 없습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 이미지 업로드
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 파일 검증
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "업로드할 파일이 없습니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 파일 크기 검증 (10MB 제한)
            if (file.getSize() > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "파일 크기는 10MB를 초과할 수 없습니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 파일 타입 검증 (이미지만 허용)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 업로드 디렉토리 경로 설정
            String uploadDirPath = getUploadDirectory();
            
            // 업로드 디렉토리 생성
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    result.put("success", false);
                    result.put("message", "업로드 디렉토리를 생성할 수 없습니다.");
                    return ResponseEntity.internalServerError().body(result);
                }
            }
            
            // 디렉토리 권한 확인
            if (!uploadDir.canWrite()) {
                result.put("success", false);
                result.put("message", "업로드 디렉토리에 쓰기 권한이 없습니다.");
                return ResponseEntity.internalServerError().body(result);
            }
            
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            File destinationFile = new File(uploadDir, fileName);
            
            try {
                file.transferTo(destinationFile);
                
                // 파일이 실제로 저장되었는지 확인
                if (!destinationFile.exists()) {
                    throw new IOException("파일이 저장되지 않았습니다.");
                }
                
            } catch (IOException e) {
                result.put("success", false);
                result.put("message", "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
                return ResponseEntity.internalServerError().body(result);
            }
            
            // 이미지 URL 생성 (웹에서 접근 가능한 경로)
            String imageUrl = "/uploads/products/" + fileName;
            
            result.put("success", true);
            result.put("message", "이미지 업로드 성공");
            result.put("imageUrl", imageUrl);
            result.put("fileName", fileName);
            result.put("filePath", destinationFile.getAbsolutePath());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "파일 업로드 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 업로드된 이미지 조회
     */
    @GetMapping("/images/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try {
            String uploadDirPath = getUploadDirectory();
            File imageFile = new File(uploadDirPath, fileName);
            
            if (!imageFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            
            // 파일 확장자에 따른 Content-Type 설정
            String contentType = "image/jpeg";
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFileName.endsWith(".webp")) {
                contentType = "image/webp";
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "max-age=3600") // 1시간 캐시
                    .body(imageBytes);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * ✅ 제품 등록 - 다른 컨트롤러와 동일한 응답 구조
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductVO productVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 제품 등록 요청 시작 ===");
            System.out.println("요청 데이터: " + productVO.toString());
            
            // 필수 필드 검증
            if (productVO.getProductName() == null || productVO.getProductName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "제품명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (productVO.getVendorName() == null || productVO.getVendorName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "브랜드명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (productVO.getCategoryMain() == null || productVO.getCategoryMain().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "카테고리는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (productVO.getCompId() == null || productVO.getCompId().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "회사코드는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (productVO.getUnit() == null || productVO.getUnit().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "단위는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 등록자 정보 검증 및 설정
            if (productVO.getRegUser() == null || productVO.getRegUser().trim().isEmpty()) {
                productVO.setRegUser("SYSTEM");
                System.out.println("등록자가 없어 SYSTEM으로 설정");
            }
            
            // 상태가 없으면 승인 대기로 설정
            if (productVO.getStatus() == null || productVO.getStatus().trim().isEmpty()) {
                productVO.setStatus("040002"); // 승인 대기
            }
            
            // 등록일이 없으면 현재 시간으로 설정
            if (productVO.getRegDate() == null) {
                productVO.setRegDate(new Date());
            }
            
            System.out.println("검증 완료된 데이터:");
            System.out.println("- 제품명: " + productVO.getProductName());
            System.out.println("- 카테고리: " + productVO.getCategoryMain());
            System.out.println("- 등록자: " + productVO.getRegUser());
            System.out.println("- 등록일: " + productVO.getRegDate());
            System.out.println("- 회사코드: " + productVO.getCompId());
            System.out.println("- 브랜드: " + productVO.getVendorName());
            System.out.println("- 단위: " + productVO.getUnit());
            
            // 제품 저장 (내부에서 제품 ID 자동 생성)
            System.out.println("🔄 제품 저장 서비스 호출...");
            int saveResult = productService.saveProduct(productVO);
            System.out.println("💾 저장 결과: " + saveResult);
            
            if (saveResult > 0) {
                // 저장 후 생성된 제품 ID 확인
                String generatedProductId = productVO.getProductId();
                System.out.println("✅ 생성된 제품 ID: " + generatedProductId);
                
                // 저장 후 조인된 데이터로 다시 조회
                ProductVO savedProduct = null;
                if (generatedProductId != null && !generatedProductId.isEmpty()) {
                    try {
                        savedProduct = productService.getProduct(generatedProductId);
                        System.out.println("📋 저장된 제품 재조회 성공");
                    } catch (Exception e) {
                        System.err.println("⚠️ 저장된 제품 재조회 실패: " + e.getMessage());
                    }
                }
                
                result.put("result_code", "SUCCESS");
                result.put("message", "제품이 성공적으로 등록되었습니다. 승인 후 판매 가능합니다.");
                result.put("data", Map.of(
                    "productId", generatedProductId,
                    "status", productVO.getStatus(),
                    "regUserName", savedProduct != null ? savedProduct.getRegUserName() : null
                ));
                
                // 로깅: 등록된 제품의 직원 정보
                System.out.println("=== 제품 등록 완료 ===");
                System.out.println("제품ID: " + generatedProductId);
                System.out.println("등록자ID: " + productVO.getRegUser());
                if (savedProduct != null) {
                    System.out.println("등록자명: " + savedProduct.getRegUserName());
                }
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 등록에 실패했습니다");
                result.put("data", null);
                System.err.println("❌ 제품 저장 실패: saveResult = " + saveResult);
            }
            
        } catch (Exception e) {
            System.err.println("=== 제품 등록 중 예외 발생 ===");
            System.err.println("예외 타입: " + e.getClass().getSimpleName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            
            result.put("result_code", "FAIL");
            result.put("message", "제품 등록 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        System.out.println("=== 제품 등록 응답 ===");
        System.out.println("결과: " + result);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 정보 수정 - 다른 컨트롤러와 동일한 응답 구조
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String productId, 
            @RequestBody ProductVO productVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 제품 존재 여부 확인
            ProductVO existingProduct = productService.getProduct(productId);
            if (existingProduct == null) {
                result.put("result_code", "FAIL");
                result.put("message", "존재하지 않는 제품입니다: " + productId);
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            productVO.setProductId(productId);
            int updateResult = productService.modifyProduct(productVO);
            
            if (updateResult > 0) {
                // 수정 후 조인된 데이터로 다시 조회
                ProductVO updatedProduct = productService.getProduct(productId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "제품이 성공적으로 수정되었습니다");
                result.put("data", Map.of(
                    "updateUserName", updatedProduct != null ? updatedProduct.getUpdateUserName() : null
                ));
                
                // 로깅: 수정된 제품의 직원 정보
                System.out.println("제품 수정 완료:");
                System.out.println("제품ID: " + productId);
                System.out.println("수정자ID: " + productVO.getUpdateUser());
                if (updatedProduct != null) {
                    System.out.println("수정자명: " + updatedProduct.getUpdateUserName());
                }
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 수정에 실패했습니다");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 수정 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 중단 API - 다른 컨트롤러와 동일한 응답 구조
     */
    @PutMapping("/{productId}/stop")
    public ResponseEntity<Map<String, Object>> stopProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 제품 존재 여부 확인
            ProductVO existingProduct = productService.getProduct(productId);
            if (existingProduct == null) {
                result.put("result_code", "FAIL");
                result.put("message", "존재하지 않는 제품입니다: " + productId);
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            String updateUser = (String) requestData.getOrDefault("updateUser", "SYSTEM");
            
            // 중단 전 제품 정보 조회 (직원 이름 포함)
            System.out.println("중단 전 제품 정보:");
            System.out.println("제품ID: " + productId);
            System.out.println("현재상태: " + existingProduct.getStatus());
            System.out.println("등록자ID: " + existingProduct.getRegUser());
            System.out.println("등록자명: " + existingProduct.getRegUserName());
            
            int stopResult = productService.stopProduct(productId, updateUser);
            
            if (stopResult > 0) {
                // 중단 후 제품 정보 조회 (처리자 이름 포함)
                ProductVO afterProduct = productService.getProduct(productId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "제품이 중단 상태로 변경되었습니다");
                result.put("data", Map.of(
                    "stopperName", afterProduct != null ? afterProduct.getUpdateUserName() : null
                ));
                
                // 로깅: 중단 처리 결과
                System.out.println("제품 중단 완료:");
                System.out.println("제품ID: " + productId);
                System.out.println("처리자ID: " + updateUser);
                if (afterProduct != null) {
                    System.out.println("처리자명: " + afterProduct.getUpdateUserName());
                    System.out.println("변경후상태: " + afterProduct.getStatus());
                }
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 중단 처리에 실패했습니다");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 중단 처리 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
            System.err.println("제품 중단 처리 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 삭제 - 다른 컨트롤러와 동일한 응답 구조
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleteResult = productService.removeProduct(productId);
            
            if (deleteResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "제품이 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 삭제에 실패했습니다");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 ID 중복 확인 - 다른 컨트롤러와 동일한 응답 구조
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductId(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = productService.isProductIdExists(productId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "exists", exists,
                "message", exists ? "이미 존재하는 제품ID입니다" : "사용 가능한 제품ID입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 ID 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 승인 API - 다른 컨트롤러와 동일한 응답 구조
     */
    @PostMapping("/{productId}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            
            // 승인 전 제품 정보 조회 (직원 이름 포함)
            ProductVO beforeProduct = productService.getProduct(productId);
            System.out.println("승인 전 제품 정보:");
            System.out.println("등록자ID: " + (beforeProduct != null ? beforeProduct.getRegUser() : "N/A"));
            System.out.println("등록자명: " + (beforeProduct != null ? beforeProduct.getRegUserName() : "N/A"));
            
            int approveResult = productService.approveProduct(productId, approver);
            
            if (approveResult > 0) {
                // 승인 후 제품 정보 조회 (수정자 이름 포함)
                ProductVO afterProduct = productService.getProduct(productId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "제품이 승인되었습니다. 이제 판매 가능합니다");
                result.put("data", Map.of(
                    "approverName", afterProduct != null ? afterProduct.getUpdateUserName() : null
                ));
                
                // 로깅: 승인 처리 결과
                System.out.println("제품 승인 완료:");
                System.out.println("제품ID: " + productId);
                System.out.println("승인자ID: " + approver);
                if (afterProduct != null) {
                    System.out.println("승인자명: " + afterProduct.getUpdateUserName());
                }
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 승인에 실패했습니다");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 승인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * ✅ 제품 반려 API - 다른 컨트롤러와 동일한 응답 구조
     */
    @PostMapping("/{productId}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            String reason = (String) requestData.get("reason");
            
            // 반려 전 제품 정보 조회 (직원 이름 포함)
            ProductVO beforeProduct = productService.getProduct(productId);
            System.out.println("반려 전 제품 정보:");
            System.out.println("등록자ID: " + (beforeProduct != null ? beforeProduct.getRegUser() : "N/A"));
            System.out.println("등록자명: " + (beforeProduct != null ? beforeProduct.getRegUserName() : "N/A"));
            
            int rejectResult = productService.rejectProduct(productId, approver, reason);
            
            if (rejectResult > 0) {
                // 반려 후 제품 정보 조회 (반려자 이름 포함)
                ProductVO afterProduct = productService.getProduct(productId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "제품 승인이 거부되었습니다");
                result.put("data", Map.of(
                    "rejecterName", afterProduct != null ? afterProduct.getUpdateUserName() : null
                ));
                
                // 로깅: 반려 처리 결과
                System.out.println("제품 반려 완료:");
                System.out.println("제품ID: " + productId);
                System.out.println("반려자ID: " + approver);
                System.out.println("반려 사유: " + reason);
                if (afterProduct != null) {
                    System.out.println("반려자명: " + afterProduct.getUpdateUserName());
                }
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "제품 승인 거부에 실패했습니다");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "제품 승인 거부 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}