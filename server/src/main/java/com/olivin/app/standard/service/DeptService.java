package com.olivin.app.standard.service;

import java.util.List;
import java.util.Map;

public interface DeptService {
    
    // 모든 부서 목록 조회
    List<DeptVO> getAllDepts();
    
    // 조건별 부서 목록 조회
    List<DeptVO> getDeptList(DeptVO deptVO);
    
    // 특정 부서 조회
    DeptVO getDept(String departmentId);
    
    // 부서 등록
    int saveDept(DeptVO deptVO);
    
    // 부서 정보 수정
    int modifyDept(DeptVO deptVO);
    
    // 부서 삭제
    int removeDept(String departmentId);
    
    // 검색 조건에 따른 부서 목록 조회
    List<DeptVO> searchDepts(Map<String, Object> searchParams);
    
    // 부서 ID 존재 여부 확인
    boolean isDeptIdExists(String departmentId);
    
    // 부서명 중복 확인
    boolean isDeptNameExists(String deptName, String excludeDeptId);
    
    // 다음 부서 ID 생성
    String getNextDeptId();
    
    // 부서 통계
    List<Map<String, Object>> getDeptStats();
    
    // 최근 등록된 부서 목록
    List<DeptVO> getRecentDepts(int limit);
    
    // 활성 상태의 부서 목록 조회
    List<DeptVO> getActiveDepts();
}