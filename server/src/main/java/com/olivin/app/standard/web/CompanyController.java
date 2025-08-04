package com.olivin.app.standard.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.standard.service.CompanyService;
import com.olivin.app.standard.service.CompanyVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class CompanyController {
    
    private final CompanyService companyService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "회사 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    // 모든 회사 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCompanies(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompanyVO> companies;
            
            // 검색 조건이 있는 경우
            if (!params.isEmpty()) {
                companies = companyService.searchCompanies(params);
            } else {
                companies = companyService.getAllCompanies();
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 유형별 조회
    @GetMapping("/type/{compType}")
    public ResponseEntity<Map<String, Object>> getCompaniesByType(@PathVariable String compType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompanyVO> companies = companyService.getCompaniesByType(compType);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 특정 회사 상세 조회
    @GetMapping("/{compId}")
    public ResponseEntity<Map<String, Object>> getCompany(@PathVariable String compId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            CompanyVO company = companyService.getCompany(compId);
            
            if (company != null) {
                result.put("result_code", "SUCCESS");
                result.put("message", "성공");
                result.put("data", company);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 정보를 찾을 수 없습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(@RequestBody CompanyVO companyVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 필수 필드 검증
            if (companyVO.getCompName() == null || companyVO.getCompName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "회사명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyVO.getCompType() == null || companyVO.getCompType().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "회사 유형은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyVO.getBizNumber() == null || companyVO.getBizNumber().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "사업자번호는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 사업자번호 중복 확인
            if (companyService.isBizNumberExists(companyVO.getBizNumber(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 사업자번호입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 회사명 중복 확인
            if (companyService.isCompanyNameExists(companyVO.getCompName(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 회사명입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int saveResult = companyService.saveCompany(companyVO);
            
            if (saveResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 등록되었습니다");
                result.put("data", Map.of("compId", companyVO.getCompId()));
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 등록에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 등록 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 정보 수정
    @PutMapping("/{compId}")
    public ResponseEntity<Map<String, Object>> updateCompany(
            @PathVariable String compId, 
            @RequestBody CompanyVO companyVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            companyVO.setCompId(compId);
            
            // 사업자번호 중복 확인 (자기 자신 제외)
            if (companyVO.getBizNumber() != null && 
                companyService.isBizNumberExists(companyVO.getBizNumber(), compId)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 사업자번호입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 회사명 중복 확인 (자기 자신 제외)
            if (companyVO.getCompName() != null && 
                companyService.isCompanyNameExists(companyVO.getCompName(), compId)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 회사명입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int updateResult = companyService.modifyCompany(companyVO);
            
            if (updateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "회사 정보가 성공적으로 수정되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 정보 수정에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 삭제
    @DeleteMapping("/{compId}")
    public ResponseEntity<Map<String, Object>> deleteCompany(@PathVariable String compId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleteResult = companyService.removeCompany(compId);
            
            if (deleteResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 삭제에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 삭제 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 ID 존재 여부 확인
    @GetMapping("/check/{compId}")
    public ResponseEntity<Map<String, Object>> checkCompanyId(@PathVariable String compId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = companyService.isCompanyIdExists(compId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "exists", exists,
                "message", exists ? "이미 존재하는 회사ID입니다" : "사용 가능한 회사ID입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 ID 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 다음 회사 ID 생성
    @GetMapping("/next-id/{compType}")
    public ResponseEntity<Map<String, Object>> getNextCompanyId(@PathVariable String compType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String nextCompId = companyService.getNextCompanyId(compType);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of("nextCompId", nextCompId));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "회사 ID 생성 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 유형별 통계
    @GetMapping("/stats/by-type")
    public ResponseEntity<Map<String, Object>> getCompanyStatsByType() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = companyService.getCompanyStatsByType();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", stats);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 최근 등록된 회사 목록
    @GetMapping("/recent/{limit}")
    public ResponseEntity<Map<String, Object>> getRecentCompanies(@PathVariable int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompanyVO> companies = companyService.getRecentCompanies(limit);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "최근 회사 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}