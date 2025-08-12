package com.olivin.app.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.example.service.EmpVO;

@Mapper
public interface EmpMapper {
    
    // 모든 사원 목록 조회
    List<EmpVO> selectAllEmps();
    
    // 조건별 사원 목록 조회
    List<EmpVO> selectEmpsByCondition(Map<String, Object> searchParams);
    
    // 활성 상태의 사원만 조회
    List<EmpVO> selectActiveEmps();
    
    // 특정 사원 조회
    EmpVO selectEmp(String employeeId);
    
    // 사원 등록
    int insertEmp(EmpVO empVO);
    
    // 사원 정보 수정
    int updateEmp(EmpVO empVO);
    
    // 사원 삭제 (실제 삭제)
    int deleteEmp(String employeeId);
    
    // 사원 비활성화 (상태 변경)
    int deactivateEmp(String employeeId);
    
    // 사원 재활성화
    int reactivateEmp(String employeeId);
    
    // 사원 ID 존재 여부 확인
    int checkEmpId(String employeeId);
    
    // 이메일 중복 확인
    int checkEmail(@Param("email") String email, @Param("excludeEmpId") String excludeEmpId);
    
    // 마지막 사원 ID 조회 (ID 자동생성용)
    String selectLastEmpId();
    
    // 회사별 사원 수 조회
    int countEmpsByCompId(@Param("compId") String compId);
    
    // 부서별 사원 수 조회
    int countEmpsByDeptId(@Param("departmentId") String departmentId);
    
    // 사원 통계 조회
    List<Map<String, Object>> selectEmpStats();
    
    // 최근 등록된 사원 조회
    List<EmpVO> selectRecentEmps(int limit);
    
    // 부서에서 사용 중인 활성 사원이 있는지 확인
    int countActiveEmpsByDeptId(@Param("departmentId") String departmentId);
}