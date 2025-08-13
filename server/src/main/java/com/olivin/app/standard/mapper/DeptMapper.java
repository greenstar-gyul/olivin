package com.olivin.app.standard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.standard.service.DeptVO;

@Mapper
public interface DeptMapper {
    
    // 모든 부서 목록 조회
    List<DeptVO> selectAllDepts();
    
    // 조건별 부서 목록 조회
    List<DeptVO> selectDeptList(DeptVO deptVO);
    
    // 검색 조건에 따른 부서 목록 조회
    List<DeptVO> selectDeptsByCondition(Map<String, Object> searchParams);
    
    // 활성 상태의 부서만 조회
    List<DeptVO> selectActiveDepts();
    
    // 특정 부서 조회
    DeptVO selectDept(String departmentId);
    
    // 부서 등록
    int insertDept(DeptVO deptVO);
    
    // 부서 정보 수정
    int updateDept(DeptVO deptVO);
    
    // 부서 삭제 (실제 삭제)
    int deleteDept(String departmentId);
    
    // 부서 ID 존재 여부 확인
    int checkDeptId(String departmentId);
    
    // 부서명 중복 확인
    int checkDeptName(@Param("deptName") String deptName, @Param("excludeDeptId") String excludeDeptId);
    
    // 마지막 부서 ID 조회 (ID 자동생성용)
    String selectLastDeptId();
    
    // 부서 통계 조회
    List<Map<String, Object>> selectDeptStats();
    
    // 최근 등록된 부서 조회
    List<DeptVO> selectRecentDepts(int limit);
    
    // ===== 참조 무결성 확인을 위한 메소드들 =====
    
    // 회사별 부서 수 조회
    int countDeptsByCompId(@Param("compId") String compId);
    
    // 부서별 사원 수 조회
    int countEmpsByDeptId(@Param("departmentId") String departmentId);
    
    // 부서에서 사용 중인 활성 사원이 있는지 확인
    int countActiveEmpsByDeptId(@Param("departmentId") String departmentId);
}