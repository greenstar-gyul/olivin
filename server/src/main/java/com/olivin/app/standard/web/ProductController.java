package com.olivin.app.standard.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private static final String UPLOAD_DIR = "uploads/products/";

    @GetMapping("/ping")
    public String ping() {
        return "서버 연결 성공!";
    }
    
    @GetMapping
    public ResponseEntity<List<ProductVO>> getAllProducts() {
        List<ProductVO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/approved")
    public ResponseEntity<List<ProductVO>> getApprovedProducts() {
        List<ProductVO> products = productService.getApprovedProducts();
        return ResponseEntity.ok(products);
    }
    
    // ✅ 제품 ID 자동생성 API 추가
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
    
    @PostMapping("/search")
    public ResponseEntity<List<ProductVO>> getProductList(@RequestBody ProductVO productVO) {
        List<ProductVO> products = productService.getProductList(productVO);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductVO>> searchProducts(@RequestParam Map<String, Object> params) {
        List<ProductVO> products = productService.searchProducts(params);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<ProductVO> getProduct(@PathVariable String productId) {
        ProductVO product = productService.getProduct(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "업로드할 파일이 없습니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (file.getSize() > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "파일 크기는 10MB를 초과할 수 없습니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(result);
            }
            
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            String imageUrl = "/uploads/products/" + fileName;
            
            result.put("success", true);
            result.put("message", "이미지 업로드 성공");
            result.put("imageUrl", imageUrl);
            result.put("fileName", fileName);
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/images/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try {
            Path imagePath = Paths.get(UPLOAD_DIR + fileName);
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageBytes = Files.readAllBytes(imagePath);
            
            String contentType = "image/jpeg";
            if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(imageBytes);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductVO productVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String productId, 
            @RequestBody ProductVO productVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            productVO.setProductId(productId);
            int updateResult = productService.modifyProduct(productVO);
            
            if (updateResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 성공적으로 수정되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "제품 수정에 실패했습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
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
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductId(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = productService.isProductIdExists(productId);
        
        result.put("exists", exists);
        result.put("message", exists ? "이미 존재하는 제품ID입니다." : "사용 가능한 제품ID입니다.");
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 승인 API
    @PostMapping("/{productId}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 제품 승인 요청 ===");
            System.out.println("ProductId: " + productId);
            System.out.println("RequestData: " + requestData);
            
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            
            int approveResult = productService.approveProduct(productId, approver);
            
            if (approveResult > 0) {
                result.put("success", true);
                result.put("message", "제품이 승인되었습니다. 이제 판매 가능합니다.");
                System.out.println("✅ 승인 성공: " + productId);
            } else {
                result.put("success", false);
                result.put("message", "제품 승인에 실패했습니다.");
                System.out.println("❌ 승인 실패: " + productId + " (DB 업데이트 결과: " + approveResult + ")");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 승인 중 오류가 발생했습니다: " + e.getMessage());
            System.err.println("❌ 승인 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 반려 API
    @PostMapping("/{productId}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
            @PathVariable String productId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 제품 반려 요청 ===");
            System.out.println("ProductId: " + productId);
            System.out.println("RequestData: " + requestData);
            
            String approver = (String) requestData.getOrDefault("approver", "SYSTEM");
            String reason = (String) requestData.get("reason");
            
            int rejectResult = productService.rejectProduct(productId, approver, reason);
            
            if (rejectResult > 0) {
                result.put("success", true);
                result.put("message", "제품 승인이 거부되었습니다.");
                System.out.println("✅ 반려 성공: " + productId);
            } else {
                result.put("success", false);
                result.put("message", "제품 승인 거부에 실패했습니다.");
                System.out.println("❌ 반려 실패: " + productId + " (DB 업데이트 결과: " + rejectResult + ")");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "제품 승인 거부 중 오류가 발생했습니다: " + e.getMessage());
            System.err.println("❌ 반려 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 테스트용 승인 API
    @GetMapping("/test-approve/{productId}")
    public ResponseEntity<Map<String, Object>> testApprove(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 테스트 승인 ===");
            
            // 1. 제품 조회
            ProductVO product = productService.getProduct(productId);
            if (product == null) {
                result.put("error", "제품을 찾을 수 없습니다: " + productId);
                return ResponseEntity.ok(result);
            }
            
            result.put("before_status", product.getStatus());
            System.out.println("승인 전 상태: " + product.getStatus());
            
            // 2. 승인 처리
            int approveResult = productService.approveProduct(productId, "TEST_USER");
            result.put("approve_result", approveResult);
            System.out.println("승인 결과: " + approveResult);
            
            // 3. 다시 조회해서 상태 확인
            ProductVO updatedProduct = productService.getProduct(productId);
            if (updatedProduct != null) {
                result.put("after_status", updatedProduct.getStatus());
                System.out.println("승인 후 상태: " + updatedProduct.getStatus());
                
                result.put("success", "040001".equals(updatedProduct.getStatus()));
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(result);
        }
    }
}