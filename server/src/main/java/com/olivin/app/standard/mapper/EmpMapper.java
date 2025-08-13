package com.olivin.app.standard.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.olivin.app.standard.service.EmpVO;

@Mapper
public interface EmpMapper {
    
    // 모든 사원 목록 조회
    List<EmpVO> selectAllEmps();
    
    // 조건별 사원 목록 조회 (회사 관리와 동일한 구조)
    List<EmpVO> selectEmpList(EmpVO empVO);
    
    // 검색 조건에 따른 사원 목록 조회 (회사 관리와 동일한 구조)
    List<EmpVO> selectEmpsByCondition(Map<String, Object> searchParams);
    
    // 활성 상태의 사원만 조회 (회사 관리와 동일한 구조)
    List<EmpVO> selectActiveEmps();
    
    // 특정 사원 조회
    EmpVO selectEmp(String employeeId);
    
    // 사원 등록
    int insertEmp(EmpVO empVO);
    
    // 사원 정보 수정
    int updateEmp(EmpVO empVO);
    
    // 사원 삭제 (실제 삭제)
    int deleteEmp(String employeeId);
    
    // 사원 ID 존재 여부 확인 (회사 관리와 동일한 구조)
    int checkEmpId(String employeeId);
    
    // 이메일 중복 확인 (회사 관리와 동일한 구조)
    int checkEmail(@Param("email") String email, @Param("excludeEmpId") String excludeEmpId);
    
    // 마지막 사원 ID 조회 (ID 자동생성용) (회사 관리와 동일한 구조)
    String selectLastEmpId();
    
    // 회사 유형별 사원 ID 생성을 위한 메서드들
    
    // 회사 ID로 회사 유형 조회
    String selectCompanyType(@Param("compId") String compId);
    
    // 특정 범위에서 마지막 사원 ID 조회
    String selectLastEmpIdByRange(@Param("rangeStart") int rangeStart, @Param("rangeEnd") int rangeEnd);
    
    // 사원 통계 조회 (회사 관리와 동일한 구조)
    List<Map<String, Object>> selectEmpStats();
    
    // 최근 등록된 사원 조회 (회사 관리와 동일한 구조)
    List<EmpVO> selectRecentEmps(int limit);
    
    // ===== 참조 무결성 확인을 위한 메소드들 (회사 관리와 동일한 구조) =====
    
    // 회사별 사원 수 조회
    int countEmpsByCompId(@Param("compId") String compId);
    
    // 부서별 사원 수 조회
    int countEmpsByDeptId(@Param("departmentId") String departmentId);
    
    // 부서에서 사용 중인 활성 사원이 있는지 확인
    int countActiveEmpsByDeptId(@Param("departmentId") String departmentId);
    
    // ===== 사원 사용 여부 확인을 위한 메소드들 (회사 관리와 동일한 패턴 추가) =====
    
    // 해당 사원과 관련된 발주서 수 조회 (담당자, 승인자 등)
    int countPurchaseOrdersByEmpId(@Param("employeeId") String employeeId);
    
    // 해당 사원과 관련된 계정원장 수 조회 (처리자, 승인자 등)
    int countAccountLedgersByEmpId(@Param("employeeId") String employeeId);
    
    // 해당 사원과 관련된 판매계획 수 조회 (담당자)
    int countSalesPlansByEmpId(@Param("employeeId") String employeeId);
    
    // 해당 사원과 관련된 재고관리 수 조회 (처리자)
    int countInventoryByEmpId(@Param("employeeId") String employeeId);

    // 모든 부서 목록 조회 (간단한 Map 형태)
    List<Map<String, Object>> selectAllDepartments();
}