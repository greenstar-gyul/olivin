package com.olivin.app.roles.web;

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

import com.olivin.app.roles.service.RolesService;
import com.olivin.app.roles.service.RolesVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*") // Vue.js 개발 서버 허용
@RequiredArgsConstructor
public class RolesController {
    
    private final RolesService roleService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("result_code", "SUCCESS");
        result.put("message", "권한 API 서버 연결 성공!");
        result.put("data", null);
        return ResponseEntity.ok(result);
    }
    
    // ========== 기존 역할 관리 API ==========
    
    // 모든 권한 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<RolesVO> roles;
            
            // 검색 조건이 있는 경우
            if (!params.isEmpty()) {
                roles = roleService.searchRoles(params);
            } else {
                roles = roleService.getAllRoles();
            }
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", roles);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 특정 권한 상세 조회
    @GetMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> getRole(@PathVariable Integer roleId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            RolesVO role = roleService.getRole(roleId);
            
            if (role != null) {
                result.put("result_code", "SUCCESS");
                result.put("message", "성공");
                result.put("data", role);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "권한 정보를 찾을 수 없습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 권한 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody RolesVO roleVO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 필수 필드 검증
            if (roleVO.getRoleName() == null || roleVO.getRoleName().trim().isEmpty()) {
                result.put("result_code", "FAIL");
                result.put("message", "권한명은 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 권한명 중복 확인
            if (roleService.isRoleNameExists(roleVO.getRoleName(), null)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 권한명입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int saveResult = roleService.saveRole(roleVO);
            
            if (saveResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "권한이 성공적으로 등록되었습니다");
                result.put("data", Map.of("roleId", roleVO.getRoleId()));
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "권한 등록에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 등록 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 권한 정보 수정
    @PutMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Integer roleId, 
            @RequestBody RolesVO roleVO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            roleVO.setRoleId(roleId);
            
            // 권한명 중복 확인 (자기 자신 제외)
            if (roleVO.getRoleName() != null && 
                roleService.isRoleNameExists(roleVO.getRoleName(), roleId)) {
                result.put("result_code", "FAIL");
                result.put("message", "이미 등록된 권한명입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int updateResult = roleService.modifyRole(roleVO);
            
            if (updateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "권한 정보가 성공적으로 수정되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "권한 정보 수정에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 권한 삭제
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Integer roleId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 권한 사용 여부 확인
            if (roleService.isRoleInUse(roleId)) {
                result.put("result_code", "FAIL");
                result.put("message", "해당 권한을 사용 중인 직원이 있어 삭제할 수 없습니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int deleteResult = roleService.removeRole(roleId);
            
            if (deleteResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "권한이 성공적으로 삭제되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "권한 삭제에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 삭제 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 모든 권한 목록 조회
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getAllPermissions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> permissions = roleService.getAllPermissions();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", permissions);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 특정 권한의 권한 ID 목록 조회 - String으로 변경
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Map<String, Object>> getPermissionIdsByRoleId(@PathVariable Integer roleId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> permissionIds = roleService.getPermissionIdsByRoleId(roleId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", permissionIds);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한별 권한 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 권한에 권한 할당 - String으로 변경
    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<Map<String, Object>> assignPermissionsToRole(
            @PathVariable Integer roleId, 
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> permissionIds = (List<String>) requestData.get("permissionIds");
            
            int assignResult = roleService.assignPermissionsToRole(roleId, permissionIds);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "권한이 성공적으로 할당되었습니다");
            result.put("data", Map.of("assignedCount", assignResult));
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 할당 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}

// ========== 추가: 사원 관련 API (같은 파일에 추가) ==========

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
class EmployeeController {
    
    private final RolesService roleService; // 기존 RolesService 재사용
    
    // 권한 관리용 사원 목록 조회 (권한 정보 포함)
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getEmployeesWithPermissions(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 기존 RolesService의 메서드를 활용하되, 사원 조회용으로 사용
            List<Map<String, Object>> employees = roleService.getEmployeesWithPermissions(params);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", employees);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ✅ 특정 사원의 권한 ID 목록 조회 - String으로 변경
    @GetMapping("/{employeeId}/permissions")
    public ResponseEntity<Map<String, Object>> getEmployeePermissions(@PathVariable String employeeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> permissionIds = roleService.getEmployeePermissionIds(employeeId);
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", permissionIds);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 권한 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // 사원의 역할 변경
    @PutMapping("/{employeeId}/role")
    public ResponseEntity<Map<String, Object>> updateEmployeeRole(
            @PathVariable String employeeId,
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer roleId = (Integer) requestData.get("roleId");
            String updateUser = (String) requestData.get("updateUser");
            
            if (roleId == null) {
                result.put("result_code", "FAIL");
                result.put("message", "역할 ID는 필수입니다");
                result.put("data", null);
                return ResponseEntity.badRequest().body(result);
            }
            
            int updateResult = roleService.updateEmployeeRole(employeeId, roleId, updateUser);
            
            if (updateResult > 0) {
                result.put("result_code", "SUCCESS");
                result.put("message", "사원 역할이 성공적으로 변경되었습니다");
                result.put("data", null);
            } else {
                result.put("result_code", "FAIL");
                result.put("message", "사원 역할 변경에 실패했습니다");
                result.put("data", null);
            }
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "사원 역할 변경 중 오류가 발생했습니다: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}

// ========== 추가: 권한 목록 API ==========

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
class PermissionController {
    
    private final RolesService roleService; // 기존 RolesService 재사용
    
    // 모든 권한 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPermissions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> permissions = roleService.getAllPermissions();
            
            result.put("result_code", "SUCCESS");
            result.put("message", "성공");
            result.put("data", permissions);
            
        } catch (Exception e) {
            result.put("result_code", "FAIL");
            result.put("message", "권한 목록 조회 실패: " + e.getMessage());
            result.put("data", null);
        }
        
        return ResponseEntity.ok(result);
    }
}