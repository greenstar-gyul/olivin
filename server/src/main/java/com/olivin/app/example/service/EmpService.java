package com.olivin.app.example.service;

import java.util.List;
import java.util.Map;

public interface EmpService {
    
    // 모든 사원 목록 조회
    List<EmpVO> getAllEmps();
    
    // 조건별 사원 목록 조회
    List<EmpVO> getEmpList(EmpVO empVO);
    
    // 특정 사원 조회
    EmpVO getEmp(String employeeId);
    
    // 사원 등록
    int saveEmp(EmpVO empVO);
    
    // 사원 정보 수정
    int modifyEmp(EmpVO empVO);
    
    // 사원 삭제
    int removeEmp(String employeeId);
    
    // 검색 조건에 따른 사원 목록 조회
    List<EmpVO> searchEmps(Map<String, Object> searchParams);
    
    // 사원 ID 존재 여부 확인
    boolean isEmpIdExists(String employeeId);
    
    // 이메일 중복 확인
    boolean isEmailExists(String email, String excludeEmpId);
    
    // 다음 사원 ID 생성
    String getNextEmpId();
    
    // 사원 통계
    List<Map<String, Object>> getEmpStats();
    
    // 최근 등록된 사원 목록
    List<EmpVO> getRecentEmps(int limit);
    
    // 활성 상태의 사원 목록 조회
    List<EmpVO> getActiveEmps();
}