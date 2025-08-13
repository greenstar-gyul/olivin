package com.olivin.app.standard.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.standard.mapper.DeptMapper;
import com.olivin.app.standard.service.DeptService;
import com.olivin.app.standard.service.DeptVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeptServiceImpl implements DeptService {
    
    private final DeptMapper deptMapper;
    
    // 부서 상태 코드 상수
    private static final String STATUS_ACTIVE = "Y";      // 활성
    private static final String STATUS_INACTIVE = "N";    // 비활성

    @Override
    public List<DeptVO> getAllDepts() {
        return deptMapper.selectAllDepts();
    }
    
    @Override
    public List<DeptVO> getDeptList(DeptVO deptVO) {
        return deptMapper.selectDeptList(deptVO);
    }
    
    @Override
    public DeptVO getDept(String departmentId) {
      return deptMapper.selectDept(departmentId);
    }
    
    @Override
    public int saveDept(DeptVO deptVO) {
        // 부서명 필수 확인
        if (deptVO.getDeptName() == null || deptVO.getDeptName().trim().isEmpty()) {
            throw new RuntimeException("부서명이 필요합니다.");
        }
        
        // 부서 ID 자동생성
        if (deptVO.getDepartmentId() == null || deptVO.getDepartmentId().isEmpty()) {
            String newDeptId = getNextDeptId();
            deptVO.setDepartmentId(newDeptId);
        }
        
        // 등록일 설정
        deptVO.setCreateDate(new Date());
        
        // 기본 상태를 활성으로 설정
        if (deptVO.getStatus() == null || deptVO.getStatus().trim().isEmpty()) {
            deptVO.setStatus(STATUS_ACTIVE);
        }
        
        return deptMapper.insertDept(deptVO);
    }
    
    @Override
    public int modifyDept(DeptVO deptVO) {
        deptVO.setUpdateDate(new Date());
        return deptMapper.updateDept(deptVO);
    }
    
    @Override
    public int removeDept(String deptId) {
        try {
            // 부서 정보 조회
            DeptVO dept = deptMapper.selectDept(deptId);
            if (dept == null) {
                throw new RuntimeException("삭제할 부서를 찾을 수 없습니다.");
            }
            
            // 부서에 속한 사원이 있는지 확인
            int empCount = deptMapper.countEmpsByDeptId(deptId);
            if (empCount > 0) {
                throw new RuntimeException("부서에 소속된 사원이 있어 삭제할 수 없습니다.");
            }
            
            // 실제 삭제
            return deptMapper.deleteDept(deptId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 부서를 비활성화 처리 (STATUS를 N으로 변경)
     */
    public int deactivateDept(String deptId) {
        try {
            DeptVO dept = deptMapper.selectDept(deptId);
            if (dept == null) {
                throw new RuntimeException("비활성화할 부서를 찾을 수 없습니다.");
            }
            
            // 이미 비활성화된 부서인지 확인
            if (STATUS_INACTIVE.equals(dept.getStatus())) {
                throw new RuntimeException("이미 비활성화된 부서입니다.");
            }
            
            // STATUS를 N으로 변경하여 비활성화 처리
            dept.setStatus(STATUS_INACTIVE);
            dept.setUpdateDate(new Date());
            
            return deptMapper.updateDept(dept);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 부서를 활성화 처리 (STATUS를 Y로 변경)
     */
    public int activateDept(String deptId) {
        try {
            DeptVO dept = deptMapper.selectDept(deptId);
            if (dept == null) {
                throw new RuntimeException("활성화할 부서를 찾을 수 없습니다.");
            }
            
            // 이미 활성화된 부서인지 확인
            if (STATUS_ACTIVE.equals(dept.getStatus())) {
                throw new RuntimeException("이미 활성화된 부서입니다.");
            }
            
            // STATUS를 Y로 변경하여 활성화 처리
            dept.setStatus(STATUS_ACTIVE);
            dept.setUpdateDate(new Date());
            
            return deptMapper.updateDept(dept);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 부서가 활성 상태인지 확인
     */
    public boolean isActiveDept(String deptId) {
        DeptVO dept = deptMapper.selectDept(deptId);
        return dept != null && dept.isActive();
    }
    
    /**
     * 활성 상태의 모든 부서 조회
     */
    @Override
    public List<DeptVO> getActiveDepts() {
        return deptMapper.selectActiveDepts();
    }
    
    @Override
    public List<DeptVO> searchDepts(Map<String, Object> searchParams) {
        return deptMapper.selectDeptsByCondition(searchParams);
    }
    
    @Override
    public boolean isDeptIdExists(String deptId) {
        return deptMapper.checkDeptId(deptId) > 0;
    }
    
    @Override
    public boolean isDeptNameExists(String deptName, String excludeDeptId) {
        return deptMapper.checkDeptName(deptName, excludeDeptId) > 0;
    }
    
    @Override
    public String getNextDeptId() {
        // 부서 ID 자동생성 로직
        String lastDeptId = deptMapper.selectLastDeptId();
        
        if (lastDeptId != null && !lastDeptId.isEmpty() && lastDeptId.startsWith("DEPT")) {
            try {
                String numPart = lastDeptId.substring(4); // "DEPT" 제거
                int lastNum = Integer.parseInt(numPart);
                int nextNum = lastNum + 1;
                return String.format("DEPT%03d", nextNum);
            } catch (NumberFormatException e) {
                return "DEPT001";
            }
        } else {
            return "DEPT001";
        }
    }
    
    @Override
    public List<Map<String, Object>> getDeptStats() {
        return deptMapper.selectDeptStats();
    }
    
    @Override
    public List<DeptVO> getRecentDepts(int limit) {
        return deptMapper.selectRecentDepts(limit);
    }
    
    /**
     * 회사별 부서 수 조회
     */
    public int countDeptsByCompId(String compId) {
        return deptMapper.countDeptsByCompId(compId);
    }
    
    /**
     * 부서에 소속된 사원 수 조회
     */
    public int countEmpsByDeptId(String departmentId) {
        return deptMapper.countEmpsByDeptId(departmentId);
    }
    
    /**
     * 부서 사용 여부 종합 확인
     * @param departmentId 부서 ID
     * @return 사용 여부 정보가 담긴 Map
     */
    public Map<String, Object> checkDeptUsage(String departmentId) {
        int empCount = deptMapper.countEmpsByDeptId(departmentId);
        boolean isUsed = empCount > 0;
        
        return Map.of(
            "isUsed", isUsed,
            "empCount", empCount,
            "details", Map.of(
                "hasEmployees", empCount > 0
            )
        );
    }
}