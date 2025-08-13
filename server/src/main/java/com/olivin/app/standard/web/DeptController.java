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

import com.olivin.app.standard.service.DeptService;
import com.olivin.app.standard.service.DeptVO;
import com.olivin.app.standard.service.impl.DeptServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/depts")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class DeptController {
    
    private final DeptService deptService;
    private final DeptServiceImpl deptServiceImpl; // 비활성화/활성화 메서드 사용을 위해 추가

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "부서 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    // 모든 부서 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDepts(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<DeptVO> departments;
            
            // 검색 조건이 있는 경우
            if (!params.isEmpty()) {
                departments = deptService.searchDepts(params);
            } else {
                departments = deptService.getAllDepts();
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", departments);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 활성 상태의 모든 부서 조회
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveDepts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<DeptVO> departments = deptServiceImpl.getActiveDepts();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", departments);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "활성 부서 목록 조회 실패: " + e.getMessage());
            result.put("data", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 특정 부서 상세 조회
    @GetMapping("/{departmentId}")
    public ResponseEntity<Map<String, Object>> getDept(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            DeptVO department = deptService.getDept(departmentId);
            
            if (department != null) {
                result.put("result_code", "SUCCESS");
                result.put("message", "성공");
                result.put("data", department);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 정보를 찾을 수 없습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDept(@RequestBody DeptVO deptVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 필수 필드 검증
            if (deptVO.getDeptName() == null || deptVO.getDeptName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "부서명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 부서명 중복 확인
            if (deptService.isDeptNameExists(deptVO.getDeptName(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 부서명입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int saveResult = deptService.saveDept(deptVO);
            
            if (saveResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "부서가 성공적으로 등록되었습니다");
                result.put("data", Map.of("departmentId", deptVO.getDepartmentId()));
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 등록에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 등록 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 정보 수정
    @PutMapping("/{departmentId}")
    public ResponseEntity<Map<String, Object>> updateDept(
            @PathVariable String departmentId, 
            @RequestBody DeptVO deptVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            deptVO.setDepartmentId(departmentId);
            
            // 부서명 중복 확인 (자기 자신 제외)
            if (deptVO.getDeptName() != null && !deptVO.getDeptName().trim().isEmpty()) {
                if (deptService.isDeptNameExists(deptVO.getDeptName(), departmentId)) {
                    result.put("result_code", "FAIL");
                    result.put("message", "이미 등록된 부서명입니다");
                    result.put("data", null);
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            int updateResult = deptService.modifyDept(deptVO);
            
            if (updateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "부서 정보가 성공적으로 수정되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 정보 수정에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 삭제
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Map<String, Object>> deleteDept(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleteResult = deptService.removeDept(departmentId);
            
            if (deleteResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "부서가 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 삭제에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 비활성화 처리 (명시적인 비활성화 API)
    @PutMapping("/{departmentId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateDept(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deactivateResult = deptServiceImpl.deactivateDept(departmentId);
            
            if (deactivateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "부서가 성공적으로 비활성화되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 비활성화에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 활성화 처리
    @PutMapping("/{departmentId}/activate")
    public ResponseEntity<Map<String, Object>> activateDept(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int activateResult = deptServiceImpl.activateDept(departmentId);
            
            if (activateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "부서가 성공적으로 활성화되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "부서 활성화에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 활성 상태 확인
    @GetMapping("/{departmentId}/active")
    public ResponseEntity<Map<String, Object>> checkDeptActive(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isActive = deptServiceImpl.isActiveDept(departmentId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "isActive", isActive,
                "message", isActive ? "활성 상태입니다" : "비활성 상태입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 사용 여부 확인
    @GetMapping("/{departmentId}/usage")
    public ResponseEntity<Map<String, Object>> checkDeptUsage(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> usageInfo = deptServiceImpl.checkDeptUsage(departmentId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", usageInfo);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 사용 여부 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 ID 존재 여부 확인
    @GetMapping("/check/{departmentId}")
    public ResponseEntity<Map<String, Object>> checkDeptId(@PathVariable String departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean exists = deptService.isDeptIdExists(departmentId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of(
                "exists", exists,
                "message", exists ? "이미 존재하는 부서ID입니다" : "사용 가능한 부서ID입니다"
            ));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 ID 확인 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 다음 부서 ID 생성
    @GetMapping("/next-id")
    public ResponseEntity<Map<String, Object>> getNextDeptId() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String nextDeptId = deptService.getNextDeptId();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", Map.of("nextDeptId", nextDeptId));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "부서 ID 생성 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 부서 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDeptStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> stats = deptService.getDeptStats();
            
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
    
    // 최근 등록된 부서 목록
    @GetMapping("/recent/{limit}")
    public ResponseEntity<Map<String, Object>> getRecentDepts(@PathVariable int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<DeptVO> departments = deptService.getRecentDepts(limit);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", departments);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "최근 부서 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}