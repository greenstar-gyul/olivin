package com.olivin.app.standard.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
     * 서버 연결 확인용 핑 API
     */
    @GetMapping("/ping")
    public String ping() {
        return "서버 연결 성공!";
    }
    
    /**
     * 모든 제품 조회
     */
    @GetMapping
    public ResponseEntity<List<ProductVO>> getAllProducts() {
        List<ProductVO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * 승인된 제품만 조회
     */
    @GetMapping("/approved")
    public ResponseEntity<List<ProductVO>> getApprovedProducts() {
        List<ProductVO> products = productService.getApprovedProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * 카테고리별 다음 제품 ID 자동생성 API
     * 실제 DB의 마지막 제품 ID를 조회하여 다음 순번 생성
     */
    @GetMapping("/next-id/{categoryMain}")
    public ResponseEntity<Map<String, Object>> getNextProductId(@PathVariable String categoryMain) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String nextProductId = productService.getNextProductId(categoryMain);
            
            if (nextProductId != null && !nextProductId.isEmpty()) {
                result.put("success", true);
                result.put("nextProductId", nextProductId);
                result.put("message", "제품 ID 생성 성공");
            } else {
                result.put("success", false);
                result.put("message", "유효하지 않은 카테고리입니다.");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 ID 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 목록 조회 (POST 방식)
     */
    @PostMapping("/search")
    public ResponseEntity<List<ProductVO>> getProductList(@RequestBody ProductVO productVO) {
        List<ProductVO> products = productService.getProductList(productVO);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 제품 검색 (GET 방식, 파라미터 기반)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductVO>> searchProducts(@RequestParam Map<String, Object> params) {
        List<ProductVO> products = productService.searchProducts(params);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 특정 제품 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductVO> getProduct(@PathVariable String productId) {
        ProductVO product = productService.getProduct(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
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
     * 제품 등록
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductVO productVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 필수 필드 검증
            if (productVO.getProductName() == null || productVO.getProductName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "제품명은 필수입니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (productVO.getVendorName() == null || productVO.getVendorName().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "브랜드명은 필수입니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 제품 ID 중복 확인
            if (productVO.getProductId() != null && !productVO.getProductId().trim().isEmpty()) {
                boolean exists = productService.isProductIdExists(productVO.getProductId());
                if (exists) {
                    result.put("success", false);
                    result.put("message", "이미 존재하는 제품 ID입니다: " + productVO.getProductId());
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            // 제품 저장
            int saveResult = productService.saveProduct(productVO);
            
            if (saveResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 성공적으로 등록되었습니다. 승인 후 판매 가능합니다.");
                result.put("productId", productVO.getProductId());
                result.put("status", productVO.getStatus());
            } else {
                result.put("success", false);
                result.put("message", "제품 등록에 실패했습니다.");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            result.put("success", false);
            if (e.getMessage().contains("unique constraint")) {
                result.put("message", "중복된 데이터가 존재합니다. 제품 ID나 제품명을 확인해주세요.");
            } else {
                result.put("message", "데이터 무결성 오류가 발생했습니다: " + e.getMessage());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 정보 수정
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
                result.put("success", false);
                result.put("message", "존재하지 않는 제품입니다: " + productId);
                return ResponseEntity.notFound().build();
            }
            
            productVO.setProductId(productId);
            int updateResult = productService.modifyProduct(productVO);
            
            if (updateResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 성공적으로 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "제품 수정에 실패했습니다.");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            result.put("success", false);
            if (e.getMessage().contains("unique constraint")) {
                result.put("message", "중복된 데이터가 존재합니다. 제품명이나 다른 정보를 확인해주세요.");
            } else {
                result.put("message", "데이터 무결성 오류가 발생했습니다: " + e.getMessage());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 삭제
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleteResult = productService.removeProduct(productId);
            
            if (deleteResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 성공적으로 삭제되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "제품 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 ID 중복 확인
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductId(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = productService.isProductIdExists(productId);
        
        result.put("exists", exists);
        result.put("message", exists ? "이미 존재하는 제품ID입니다." : "사용 가능한 제품ID입니다.");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 승인 API
     */
    @PostMapping("/{productId}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            
            int approveResult = productService.approveProduct(productId, approver);
            
            if (approveResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 승인되었습니다. 이제 판매 가능합니다.");
            } else {
                result.put("success", false);
                result.put("message", "제품 승인에 실패했습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 제품 반려 API
     */
    @PostMapping("/{productId}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            String reason = (String) requestData.get("reason");
            
            int rejectResult = productService.rejectProduct(productId, approver, reason);
            
            if (rejectResult > 0) {
                result.put("success", true);
                result.put("message", "제품 승인이 거부되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "제품 승인 거부에 실패했습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 승인 거부 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}