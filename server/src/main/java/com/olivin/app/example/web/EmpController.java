package com.olivin.app.example.web;

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

import com.olivin.app.example.service.EmpService;
import com.olivin.app.example.service.EmpVO;
import com.olivin.app.example.service.impl.EmpServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class EmpController {
    
    private final EmpService empService;
    private final EmpServiceImpl empServiceImpl; // 비활성화 메서드 사용을 위해 추가

    // ===== 기존 API 호환성을 위한 엔드포인트 =====
    @GetMapping("/public/emps/search")
    public ResponseEntity<List<EmpVO>> empList() {
        try {
            System.out.println("사원 목록 조회 API 호출됨");
            List<EmpVO> employees = empService.getAllEmps();
            System.out.println("조회된 사원 수: " + (employees != null ? employees.size() : 0));
            return ResponseEntity.ok(employees != null ? employees : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("사원 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/public/emps")
    public ResponseEntity<List<EmpVO>> searchEmps(@RequestParam(required = false) String searchValue) {
        try {
            System.out.println("사원 검색 API 호출됨, 검색어: " + searchValue);
            List<EmpVO> employees;
            
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                // 간단한 검색 파라미터를 Map으로 변환
                Map<String, Object> params = new HashMap<>();
                params.put("empName", searchValue);
                employees = empService.searchEmps(params);
            } else {
                employees = empService.getAllEmps();
            }
            
            System.out.println("검색된 사원 수: " + (employees != null ? employees.size() : 0));
            return ResponseEntity.ok(employees != null ? employees : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("사원 검색 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/public/emps")
    public ResponseEntity<Integer> insertEmp(@RequestBody EmpVO empVO) {
        try {
            System.out.println("사원 등록 API 호출됨: " + empVO.getEmpName());
            int result = empService.saveEmp(empVO);
            System.out.println("사원 등록 결과: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("사원 등록 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(0);
        }
    }
    
    @PutMapping("/public/emps/{employeeId}")
    public ResponseEntity<Integer> updateEmpPublic(@PathVariable String employeeId, @RequestBody EmpVO empVO) {
        try {
            System.out.println("사원 수정 API 호출됨: " + employeeId);
            empVO.setEmployeeId(employeeId);
            int result = empService.modifyEmp(empVO);
            System.out.println("사원 수정 결과: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("사원 수정 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(0);
        }
    }
    
    @DeleteMapping("/public/emps")
    public ResponseEntity<Integer> deleteEmpPublic(@RequestParam String employeeId) {
        try {
            System.out.println("사원 삭제 API 호출됨: " + employeeId);
            int result = empService.removeEmp(employeeId);
            System.out.println("사원 삭제 결과: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("사원 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(0);
        }
    }

    // ===== 새로운 표준 API 엔드포인트 =====
    @GetMapping("/employees/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "사원 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    // 모든 사원 목록 조회
    @GetMapping("/employees")
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
    
    // 활성 상태의 모든 사원 조회
    @GetMapping("/employees/active")
    public ResponseEntity<Map<String, Object>> getActiveEmps() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<EmpVO> employees = empService.getActiveEmps();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", employees);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "활성 사원 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 특정 사원 상세 조회
    @GetMapping("/employees/{employeeId}")
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
    @PostMapping("/employees")
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
                result.put("message", "회사코드는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (empVO.getDepartmentId() == null || empVO.getDepartmentId().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "부서코드는 필수입니다");
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
    @PutMapping("/employees/{employeeId}")
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
    
    // 사원 삭제 (비활성화)
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<Map<String, Object>> deleteEmp(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleteResult = empService.removeEmp(employeeId);
            
            if (deleteResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원이 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 삭제에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 다음 사원 ID 생성
    @GetMapping("/employees/next-id")
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
}