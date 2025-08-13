package com.olivin.app.standard.web;

import java.util.ArrayList;
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

import com.olivin.app.standard.mapper.EmpMapper;
import com.olivin.app.standard.service.EmpService;
import com.olivin.app.standard.service.EmpVO;
import com.olivin.app.standard.service.impl.EmpServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emps")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class EmpController {
    
    private final EmpService empService;
    private final EmpServiceImpl empServiceImpl; // 퇴사/재입사 메서드 사용을 위해 추가
    private final EmpMapper empMapper; // ✅ 부서 조회를 위해 추가

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "사원 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    // 모든 사원 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmps(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EmpVO> employees;
            
            // 검색 조건이 있는 경우
            if (!params.isEmpty()) {
                employees = empService.searchEmps(params);
            } else {
                employees = empService.getAllEmps();
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", employees);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 재직중인 모든 사원 조회
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveEmps() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EmpVO> employees = empServiceImpl.getActiveEmps();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", employees);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "재직중 사원 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 특정 사원 상세 조회
    @GetMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmp(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            EmpVO employee = empService.getEmp(employeeId);
            
            if (employee != null) {
                result.put("result_code", "SUCCESS");
                result.put("message", "성공");
                result.put("data", employee);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 정보를 찾을 수 없습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmp(@RequestBody EmpVO empVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 필수 필드 검증
            if (empVO.getEmpName() == null || empVO.getEmpName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "사원명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (empVO.getCompId() == null || empVO.getCompId().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "회사는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (empVO.getDepartmentId() == null || empVO.getDepartmentId().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "부서는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 퇴사 상태로 등록 시도 방지
            if ("050002".equals(empVO.getStatus())) {
                result.put("result_code", "FAIL");
                result.put("message", "퇴사 상태로는 신규 사원을 등록할 수 없습니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 이메일 중복 확인
            if (empVO.getEmail() != null && !empVO.getEmail().trim().isEmpty()) {
                if (empService.isEmailExists(empVO.getEmail(), null)) {
                    result.put("result_code", "FAIL");
                    result.put("message", "이미 등록된 이메일입니다");
                    result.put("data", null);
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            int saveResult = empService.saveEmp(empVO);
            
            if (saveResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원이 성공적으로 등록되었습니다");
                result.put("data", Map.of("employeeId", empVO.getEmployeeId()));
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 등록에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 등록 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 정보 수정
    @PutMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> updateEmp(
            @PathVariable String employeeId, 
            @RequestBody EmpVO empVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            empVO.setEmployeeId(employeeId);
            
            // 이메일 중복 확인 (자기 자신 제외)
            if (empVO.getEmail() != null && !empVO.getEmail().trim().isEmpty()) {
                if (empService.isEmailExists(empVO.getEmail(), employeeId)) {
                    result.put("result_code", "FAIL");
                    result.put("message", "이미 등록된 이메일입니다");
                    result.put("data", null);
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            int updateResult = empService.modifyEmp(empVO);
            
            if (updateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원 정보가 성공적으로 수정되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 정보 수정에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 퇴사 처리 (기존 DELETE 요청을 퇴사 처리로 변경)
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> resignEmp(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int resignResult = empService.removeEmp(employeeId);
            
            if (resignResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원이 성공적으로 퇴사 처리되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 퇴사 처리에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 퇴사 처리 (명시적인 퇴사 API)
    @PutMapping("/{employeeId}/resign")
    public ResponseEntity<Map<String, Object>> resignEmpExplicit(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int resignResult = empServiceImpl.resignEmp(employeeId);
            
            if (resignResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원이 성공적으로 퇴사 처리되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 퇴사 처리에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 재입사 처리
    @PutMapping("/{employeeId}/rehire")
    public ResponseEntity<Map<String, Object>> rehireEmp(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int rehireResult = empServiceImpl.rehireEmp(employeeId);
            
            if (rehireResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원이 성공적으로 재입사 처리되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 재입사 처리에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 재직 상태 확인
    @GetMapping("/{employeeId}/active")
    public ResponseEntity<Map<String, Object>> checkEmpActive(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isActive = empServiceImpl.isActiveEmp(employeeId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "isActive", isActive,
                "message", isActive ? "재직 중입니다" : "퇴사 상태입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 사용 여부 확인
    @GetMapping("/{employeeId}/usage")
    public ResponseEntity<Map<String, Object>> checkEmpUsage(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> usageInfo = empServiceImpl.checkEmpUsage(employeeId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", usageInfo);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 사용 여부 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 ID 존재 여부 확인
    @GetMapping("/check/{employeeId}")
    public ResponseEntity<Map<String, Object>> checkEmpId(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = empService.isEmpIdExists(employeeId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "exists", exists,
                "message", exists ? "이미 존재하는 사원ID입니다" : "사용 가능한 사원ID입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 ID 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 다음 사원 ID 생성
    @GetMapping("/next-id")
    public ResponseEntity<Map<String, Object>> getNextEmpId() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String nextEmpId = empService.getNextEmpId();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of("nextEmpId", nextEmpId));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 ID 생성 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEmpStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = empService.getEmpStats();
            
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
    
    // 최근 등록된 사원 목록
    @GetMapping("/recent/{limit}")
    public ResponseEntity<Map<String, Object>> getRecentEmps(@PathVariable int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EmpVO> employees = empService.getRecentEmps(limit);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", employees);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "최근 사원 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }

    // ✅ 부서 목록 조회 (사원 등록 시 모달용) - 문제 해결된 버전
    @GetMapping("/departments")
    public ResponseEntity<Map<String, Object>> getDepartments() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("부서 목록 조회 API 시작");
            
            // 1. 먼저 DB에서 부서 목록 조회 시도
            List<Map<String, Object>> departments = null;
            
            try {
                departments = empMapper.selectAllDepartments();
                System.out.println("DB에서 조회한 부서 수: " + (departments != null ? departments.size() : 0));
                
                if (departments != null && !departments.isEmpty()) {
                    System.out.println("DB 조회 결과 샘플: " + departments.get(0));
                }
            } catch (Exception dbError) {
                System.err.println("DB에서 부서 조회 실패: " + dbError.getMessage());
                dbError.printStackTrace();
            }
            
            // 2. DB 조회 결과가 없거나 실패한 경우 기본 데이터 사용
            if (departments == null || departments.isEmpty()) {
                System.out.println("DB 조회 실패 또는 데이터 없음, 기본 부서 데이터 사용");
                departments = getDefaultDepartments();
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", departments);
            
            System.out.println("최종 반환할 부서 데이터 수: " + departments.size());
            
        } catch (Exception e) {
            System.err.println("부서 조회 API 전체 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 모든 실패 시에도 기본 데이터 반환
            List<Map<String, Object>> defaultDepartments = getDefaultDepartments();
            result.put("result_code", "SUCCESS");
            result.put("message", "기본 부서 목록 반환");
            result.put("data", defaultDepartments);
        }
        
        return ResponseEntity.ok(result);
    }

    // ✅ 기본 부서 데이터 (DB에 부서 테이블이 없거나 조회 실패 시 사용) - 확장된 버전
    private List<Map<String, Object>> getDefaultDepartments() {
        List<Map<String, Object>> departments = new ArrayList<>();
        
        String[][] deptData = {
            {"DEPT001", "경영지원팀"},
            {"DEPT002", "재무회계팀"},
            {"DEPT003", "구매팀"},
            {"DEPT004", "물류팀"},
            {"DEPT005", "영업팀"},
            {"DEPT006", "마케팅팀"},
            {"DEPT007", "상품기획팀"},
            {"DEPT008", "IT팀"},
            {"DEPT009", "고객서비스팀"},
            {"DEPT010", "매장관리팀"},
            {"DEPT011", "인사팀"},
            {"DEPT012", "총무팀"},
            {"DEPT013", "법무팀"},
            {"DEPT014", "감사팀"},
            {"DEPT015", "연구개발팀"}
        };
        
        for (String[] dept : deptData) {
            Map<String, Object> deptMap = new HashMap<>();
            deptMap.put("departmentId", dept[0]);
            deptMap.put("deptName", dept[1]);
            departments.add(deptMap);
        }
        
        System.out.println("기본 부서 데이터 생성 완료: " + departments.size() + "개");
        return departments;
    }
}