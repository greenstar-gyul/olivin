package com.olivin.app.example.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.example.mapper.EmpMapper;
import com.olivin.app.example.service.EmpService;
import com.olivin.app.example.service.EmpVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpServiceImpl implements EmpService {
    
    private final EmpMapper empMapper;
    
    // 사원 상태 코드 상수
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String STATUS_LEAVE = "LEAVE";

    @Override
    public List<EmpVO> getAllEmps() {
        return empMapper.selectAllEmps();
    }
    
    @Override
    public List<EmpVO> getEmpList(EmpVO empVO) {
        // 기존 메서드 호환성을 위해 유지
        return empMapper.selectAllEmps();
    }
    
    @Override
    public EmpVO getEmp(String employeeId) {
        return empMapper.selectEmp(employeeId);
    }
    
    @Override
    public int saveEmp(EmpVO empVO) {
        // 사원 ID 자동생성 (olivin + 5자리 순번)
        if (empVO.getEmployeeId() == null || empVO.getEmployeeId().isEmpty()) {
            String newEmpId = getNextEmpId();
            empVO.setEmployeeId(newEmpId);
        }
        
        // 등록일 설정
        empVO.setCreateDate(new Date());
        
        // 입사일이 없으면 등록일로 설정
        if (empVO.getHireDate() == null) {
            empVO.setHireDate(new Date());
        }
        
        // 기본 상태를 ACTIVE로 설정
        if (empVO.getStatus() == null || empVO.getStatus().isEmpty()) {
            empVO.setStatus(STATUS_ACTIVE);
        }
        
        // 기본 역할 ID 설정 (필요시)
        if (empVO.getRoleId() == null) {
            empVO.setRoleId(1); // 기본 역할
        }
        
        return empMapper.insertEmp(empVO);
    }
    
    @Override
    public int modifyEmp(EmpVO empVO) {
        empVO.setUpdateDate(new Date());
        return empMapper.updateEmp(empVO);
    }
    
    @Override
    public int removeEmp(String employeeId) {
        try {
            // 사원 정보 조회
            EmpVO emp = empMapper.selectEmp(employeeId);
            if (emp == null) {
                throw new RuntimeException("삭제할 사원을 찾을 수 없습니다.");
            }
            
            // 이미 비활성화된 사원인지 확인
            if (STATUS_INACTIVE.equals(emp.getStatus())) {
                throw new RuntimeException("이미 퇴사한 사원입니다.");
            }
            
            // 다른 테이블에서 참조하고 있는지 확인 (발주서, 계정원장 등)
            // 필요시 관련 테이블 확인 로직 추가
            
            // 실제 삭제 대신 비활성화로 처리
            return deactivateEmp(employeeId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원을 비활성화 처리 (STATUS를 INACTIVE로 변경)
     */
    public int deactivateEmp(String employeeId) {
        try {
            EmpVO emp = empMapper.selectEmp(employeeId);
            if (emp == null) {
                throw new RuntimeException("비활성화할 사원을 찾을 수 없습니다.");
            }
            
            // 이미 비활성화된 사원인지 확인
            if (STATUS_INACTIVE.equals(emp.getStatus())) {
                throw new RuntimeException("이미 퇴사한 사원입니다.");
            }
            
            return empMapper.deactivateEmp(employeeId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원을 다시 활성화 처리 (STATUS를 ACTIVE로 변경)
     */
    public int reactivateEmp(String employeeId) {
        try {
            EmpVO emp = empMapper.selectEmp(employeeId);
            if (emp == null) {
                throw new RuntimeException("활성화할 사원을 찾을 수 없습니다.");
            }
            
            // 비활성화된 사원인지 확인
            if (!STATUS_INACTIVE.equals(emp.getStatus())) {
                throw new RuntimeException("이미 재직 중인 사원입니다.");
            }
            
            return empMapper.reactivateEmp(employeeId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원이 활성 상태인지 확인
     */
    public boolean isActiveEmp(String employeeId) {
        EmpVO emp = empMapper.selectEmp(employeeId);
        return emp != null && emp.isActive();
    }
    
    /**
     * 활성 상태의 모든 사원 조회
     */
    @Override
    public List<EmpVO> getActiveEmps() {
        return empMapper.selectActiveEmps();
    }
    
    @Override
    public List<EmpVO> searchEmps(Map<String, Object> searchParams) {
        return empMapper.selectEmpsByCondition(searchParams);
    }
    
    @Override
    public boolean isEmpIdExists(String employeeId) {
        return empMapper.checkEmpId(employeeId) > 0;
    }
    
    @Override
    public boolean isEmailExists(String email, String excludeEmpId) {
        return empMapper.checkEmail(email, excludeEmpId) > 0;
    }
    
    @Override
    public String getNextEmpId() {
        try {
            String lastEmpId = empMapper.selectLastEmpId();
            if (lastEmpId != null && !lastEmpId.isEmpty() && lastEmpId.startsWith("olivin")) {
                // olivin10001 형태에서 숫자 부분 추출
                String numPart = lastEmpId.substring(6); // "olivin" 다음 부분
                int lastNum = Integer.parseInt(numPart);
                int nextNum = lastNum + 1;
                
                // 명세서에 따른 범위 확인
                if (nextNum >= 10001 && nextNum <= 39999) {
                    // 본사 직원 범위
                    return "olivin" + String.format("%05d", nextNum);
                } else if (nextNum >= 40001 && nextNum <= 69999) {
                    // 지점 직원 범위
                    return "olivin" + String.format("%05d", nextNum);
                } else if (nextNum >= 70001 && nextNum <= 99999) {
                    // 공급업체 직원 범위
                    return "olivin" + String.format("%05d", nextNum);
                } else {
                    // 범위 초과 시 본사 직원 시작 번호로
                    return "olivin10001";
                }
            } else {
                // 첫 번째 사원은 본사 직원으로 시작
                return "olivin10001";
            }
        } catch (Exception e) {
            System.err.println("사원 ID 생성 중 오류: " + e.getMessage());
            // 기본값 반환
            return "olivin10001";
        }
    }
    
    /**
     * 회사 유형에 따른 사원 ID 생성
     * @param compType 회사 유형 (100001=본사, 100002=지점, 100003=공급업체)
     * @return 다음 사원 ID
     */
    public String getNextEmpIdByCompType(String compType) {
        try {
            String prefix = "olivin";
            int startRange, endRange;
            
            switch (compType) {
                case "100001": // 본사
                    startRange = 10001;
                    endRange = 39999;
                    break;
                case "100002": // 지점
                    startRange = 40001;
                    endRange = 69999;
                    break;
                case "100003": // 공급업체
                    startRange = 70001;
                    endRange = 99999;
                    break;
                default:
                    startRange = 10001;
                    endRange = 39999;
                    break;
            }
            
            // 해당 범위의 마지막 사원 ID 조회
            String lastEmpId = empMapper.selectLastEmpId();
            
            if (lastEmpId != null && !lastEmpId.isEmpty() && lastEmpId.startsWith(prefix)) {
                String numPart = lastEmpId.substring(6);
                int lastNum = Integer.parseInt(numPart);
                
                // 같은 범위 내에서 다음 번호 생성
                if (lastNum >= startRange && lastNum < endRange) {
                    int nextNum = lastNum + 1;
                    return prefix + String.format("%05d", nextNum);
                }
            }
            
            // 해당 범위의 첫 번째 번호 반환
            return prefix + String.format("%05d", startRange);
            
        } catch (Exception e) {
            System.err.println("회사 유형별 사원 ID 생성 중 오류: " + e.getMessage());
            return "olivin10001";
        }
    }
    
    @Override
    public List<Map<String, Object>> getEmpStats() {
        return empMapper.selectEmpStats();
    }
    
    @Override
    public List<EmpVO> getRecentEmps(int limit) {
        return empMapper.selectRecentEmps(limit);
    }
    
    /**
     * 회사별 사원 수 조회
     */
    public int countEmpsByCompId(String compId) {
        return empMapper.countEmpsByCompId(compId);
    }
    
    /**
     * 부서별 사원 수 조회
     */
    public int countEmpsByDeptId(String departmentId) {
        return empMapper.countEmpsByDeptId(departmentId);
    }
    
    /**
     * 부서에서 사용 중인 활성 사원이 있는지 확인
     */
    public boolean hasActiveEmpsInDept(String departmentId) {
        return empMapper.countActiveEmpsByDeptId(departmentId) > 0;
    }
}