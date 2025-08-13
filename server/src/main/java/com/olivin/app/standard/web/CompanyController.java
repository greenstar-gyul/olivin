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
import com.olivin.app.standard.service.impl.CompanyServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class CompanyController {
    
    private final CompanyService companyService;
    private final CompanyServiceImpl companyServiceImpl; // 비활성화 메서드 사용을 위해 추가

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
            System.out.println("=== 회사 목록 조회 요청 ===");
            System.out.println("요청 파라미터: " + params);
            
            List<CompanyVO> companies;
            
            // 검색 조건이 있는 경우
            if (!params.isEmpty()) {
                companies = companyService.searchCompanies(params);
            } else {
                companies = companyService.getAllCompanies();
            }
            
            System.out.println("조회된 회사 수: " + companies.size());
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            System.err.println("회사 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            
            result.put("result_code", "FAIL");
            result.put("message", "회사 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 활성 상태의 모든 회사 조회
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCompanies() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompanyVO> companies = companyServiceImpl.getActiveCompanies();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            System.err.println("활성 회사 목록 조회 실패: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "활성 회사 목록 조회 실패: " + e.getMessage());
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
            System.err.println("회사 유형별 조회 실패: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "회사 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 활성 상태의 회사 유형별 조회
    @GetMapping("/active/type/{compType}")
    public ResponseEntity<Map<String, Object>> getActiveCompaniesByType(@PathVariable String compType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompanyVO> companies = companyServiceImpl.getActiveCompaniesByType(compType);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", companies);
            
        } catch (Exception e) {
            System.err.println("활성 회사 유형별 조회 실패: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "활성 회사 목록 조회 실패: " + e.getMessage());
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
            System.err.println("회사 조회 실패: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "회사 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 회사 등록 - 강화된 디버깅과 오류 처리
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(@RequestBody CompanyVO companyVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 회사 등록 요청 시작 ===");
            System.out.println("요청 데이터: " + companyVO.toString());
            
            // ✅ null 체크 및 기본 검증
            if (companyVO == null) {
                result.put("result_code", "FAIL");
                result.put("message", "요청 데이터가 없습니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // ✅ 문자열 날짜 처리
            if (companyVO.getRegDateStr() != null && !companyVO.getRegDateStr().trim().isEmpty()) {
                companyVO.setRegDateFromString(companyVO.getRegDateStr());
                System.out.println("등록일 문자열 변환: " + companyVO.getRegDateStr() + " → " + companyVO.getRegDate());
            }
            
            // ✅ 필수 필드 검증 (서버 사이드)
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
            
            // 비활성화 타입으로 등록 시도 방지
            if ("FFFFFF".equals(companyVO.getCompType())) {
                result.put("result_code", "FAIL");
                result.put("message", "비활성화 상태로는 신규 회사를 등록할 수 없습니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyVO.getBizNumber() == null || companyVO.getBizNumber().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "사업자번호는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyVO.getCeoName() == null || companyVO.getCeoName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "CEO명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // ✅ 중복 검증
            if (companyService.isBizNumberExists(companyVO.getBizNumber(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 사업자번호입니다: " + companyVO.getBizNumber());
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyService.isCompanyNameExists(companyVO.getCompName(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 회사명입니다: " + companyVO.getCompName());
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // ✅ 회사 등록 실행
            int saveResult = companyService.saveCompany(companyVO);
            
            if (saveResult > 0) {
                System.out.println("회사 등록 성공 - ID: " + companyVO.getCompId());
                
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 등록되었습니다");
                result.put("data", Map.of(
                    "compId", companyVO.getCompId(),
                    "compName", companyVO.getCompName(),
                    "compType", companyVO.getCompType()
                ));
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 등록에 실패했습니다 (저장 결과: " + saveResult + ")");
                result.put("data", null);
            }
            
            System.out.println("=== 회사 등록 요청 완료 ===");
            
        } catch (Exception e) {
            System.err.println("회사 등록 중 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "회사 등록 중 알 수 없는 오류가 발생했습니다";
            }
            
            result.put("result_code", "FAIL");
            result.put("message", errorMessage);
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 회사 정보 수정 - 강화된 처리
    @PutMapping("/{compId}")
    public ResponseEntity<Map<String, Object>> updateCompany(
            @PathVariable String compId, 
            @RequestBody CompanyVO companyVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 회사 수정 요청 시작: " + compId + " ===");
            System.out.println("수정 데이터: " + companyVO.toString());
            
            companyVO.setCompId(compId);
            
            // ✅ 문자열 날짜 처리
            if (companyVO.getRegDateStr() != null && !companyVO.getRegDateStr().trim().isEmpty()) {
                companyVO.setRegDateFromString(companyVO.getRegDateStr());
            }
            if (companyVO.getUpdateDateStr() != null && !companyVO.getUpdateDateStr().trim().isEmpty()) {
                companyVO.setUpdateDateFromString(companyVO.getUpdateDateStr());
            }
            
            // ✅ 중복 검증 (자기 자신 제외)
            if (companyVO.getBizNumber() != null && !companyVO.getBizNumber().trim().isEmpty() &&
                companyService.isBizNumberExists(companyVO.getBizNumber(), compId)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 사업자번호입니다: " + companyVO.getBizNumber());
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (companyVO.getCompName() != null && !companyVO.getCompName().trim().isEmpty() &&
                companyService.isCompanyNameExists(companyVO.getCompName(), compId)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 회사명입니다: " + companyVO.getCompName());
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // ✅ 회사 수정 실행
            int updateResult = companyService.modifyCompany(companyVO);
            
            if (updateResult > 0) {
                System.out.println("회사 수정 성공: " + compId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "회사 정보가 성공적으로 수정되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 정보 수정에 실패했습니다 (수정 결과: " + updateResult + ")");
                result.put("data", null);
            }
            
            System.out.println("=== 회사 수정 요청 완료 ===");
            
        } catch (Exception e) {
            System.err.println("회사 수정 중 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("=== 회사 삭제 요청: " + compId + " ===");
            
            int deleteResult = companyService.removeCompany(compId);
            
            if (deleteResult > 0) {
                System.out.println("회사 삭제 성공: " + compId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 삭제에 실패했습니다 (삭제 결과: " + deleteResult + ")");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            System.err.println("회사 삭제 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 회사 비활성화
    @PutMapping("/{compId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateCompany(
            @PathVariable String compId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 회사 비활성화 요청: " + compId + " ===");
            
            // ✅ 요청 본문에서 사용자 정보 추출 (있는 경우)
            if (requestBody != null && requestBody.containsKey("updateUser")) {
                String updateUser = (String) requestBody.get("updateUser");
                System.out.println("비활성화 처리자: " + updateUser);
            }
            
            int deactivateResult = companyServiceImpl.deactivateCompany(compId);
            
            if (deactivateResult > 0) {
                System.out.println("회사 비활성화 성공: " + compId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 비활성화되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 비활성화에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            System.err.println("회사 비활성화 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 재활성화
    @PutMapping("/{compId}/reactivate/{compType}")
    public ResponseEntity<Map<String, Object>> reactivateCompany(
            @PathVariable String compId, 
            @PathVariable String compType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 회사 재활성화 요청: " + compId + " → " + compType + " ===");
            
            int reactivateResult = companyServiceImpl.reactivateCompany(compId, compType);
            
            if (reactivateResult > 0) {
                System.out.println("회사 재활성화 성공: " + compId);
                
                result.put("result_code", "SUCCESS");
                result.put("message", "회사가 성공적으로 재활성화되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "회사 재활성화에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            System.err.println("회사 재활성화 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 회사 활성 상태 확인
    @GetMapping("/{compId}/active")
    public ResponseEntity<Map<String, Object>> checkCompanyActive(@PathVariable String compId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isActive = companyServiceImpl.isActiveCompany(compId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "isActive", isActive,
                "message", isActive ? "활성 상태입니다" : "비활성 상태입니다"
            ));
            
        } catch (Exception e) {
            System.err.println("회사 상태 확인 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "회사 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 회사 사용 여부 확인 - 강화된 처리
    @GetMapping("/{compId}/usage")
    public ResponseEntity<Map<String, Object>> checkCompanyUsage(@PathVariable String compId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 회사 사용 여부 확인: " + compId + " ===");
            
            Map<String, Object> usageInfo = companyServiceImpl.checkCompanyUsage(compId);
            
            System.out.println("사용 여부 결과: " + usageInfo);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", usageInfo);
            
        } catch (Exception e) {
            System.err.println("회사 사용 여부 확인 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "회사 사용 여부 확인 중 오류가 발생했습니다: " + e.getMessage());
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
            System.err.println("회사 ID 확인 중 예외 발생: " + e.getMessage());
            
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
            System.out.println("=== 다음 회사 ID 생성 요청: " + compType + " ===");
            
            String nextCompId = companyService.getNextCompanyId(compType);
            
            System.out.println("생성된 다음 ID: " + nextCompId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of("nextCompId", nextCompId));
            
        } catch (Exception e) {
            System.err.println("회사 ID 생성 중 예외 발생: " + e.getMessage());
            
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
            System.err.println("통계 조회 중 예외 발생: " + e.getMessage());
            
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
            System.err.println("최근 회사 목록 조회 중 예외 발생: " + e.getMessage());
            
            result.put("result_code", "FAIL");
            result.put("message", "최근 회사 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}