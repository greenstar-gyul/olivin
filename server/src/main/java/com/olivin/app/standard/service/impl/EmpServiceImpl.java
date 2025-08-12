package com.olivin.app.standard.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.standard.mapper.EmpMapper;
import com.olivin.app.standard.service.EmpService;
import com.olivin.app.standard.service.EmpVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpServiceImpl implements EmpService {
    
    private final EmpMapper empMapper;
    
    // 사원 상태 코드 상수
    private static final String STATUS_ACTIVE = "050001";      // 재직중
    private static final String STATUS_RESIGNED = "050002";    // 퇴사

    @Override
    public List<EmpVO> getAllEmps() {
        return empMapper.selectAllEmps();
    }
    
    @Override
    public List<EmpVO> getEmpList(EmpVO empVO) {
        return empMapper.selectEmpList(empVO);
    }
    
    @Override
    public EmpVO getEmp(String employeeId) {
        return empMapper.selectEmp(employeeId);
    }
    
    @Override
    public int saveEmp(EmpVO empVO) {
        // 퇴사 상태로 등록 시도 방지
        if (STATUS_RESIGNED.equals(empVO.getStatus())) {
            throw new RuntimeException("퇴사 상태로는 새 사원을 등록할 수 없습니다.");
        }
        
        // 회사 ID 필수 확인
        if (empVO.getCompId() == null || empVO.getCompId().trim().isEmpty()) {
            throw new RuntimeException("회사 정보가 필요합니다.");
        }
        
        // 사원 ID 자동생성 (회사 유형별)
        if (empVO.getEmployeeId() == null || empVO.getEmployeeId().isEmpty()) {
            String newEmpId = getNextEmpId(empVO.getCompId());
            empVO.setEmployeeId(newEmpId);
        }
        
        // 등록일 설정
        empVO.setCreateDate(new Date());
        
        // 입사일이 없으면 등록일로 설정
        if (empVO.getHireDate() == null) {
            empVO.setHireDate(new Date());
        }
        
        // 기본 상태를 재직중으로 설정 (null이거나 빈 문자열인 경우)
        if (empVO.getStatus() == null || empVO.getStatus().trim().isEmpty()) {
            empVO.setStatus(STATUS_ACTIVE);
        }
        
        // 기본 역할 ID 설정
        if (empVO.getRoleId() == null) {
            empVO.setRoleId(1);
        }
        
        // 성별 기본값 설정 (null인 경우)
        if (empVO.getGender() == null || empVO.getGender().trim().isEmpty()) {
            empVO.setGender("M");
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
                throw new RuntimeException("퇴사 처리할 사원을 찾을 수 없습니다.");
            }
            
            // 이미 퇴사한 사원인지 확인
            if (STATUS_RESIGNED.equals(emp.getStatus())) {
                throw new RuntimeException("이미 퇴사한 사원입니다.");
            }
            
            // 퇴사 처리
            return resignEmp(employeeId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원 퇴사 처리 (STATUS를 050002로 변경)
     */
    public int resignEmp(String employeeId) {
        try {
            EmpVO emp = empMapper.selectEmp(employeeId);
            if (emp == null) {
                throw new RuntimeException("퇴사 처리할 사원을 찾을 수 없습니다.");
            }
            
            // 이미 퇴사한 사원인지 확인
            if (STATUS_RESIGNED.equals(emp.getStatus())) {
                throw new RuntimeException("이미 퇴사한 사원입니다.");
            }
            
            // STATUS를 050002로 변경하여 퇴사 처리
            emp.setStatus(STATUS_RESIGNED);
            emp.setResignDate(new Date());
            emp.setUpdateDate(new Date());
            
            return empMapper.updateEmp(emp);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원을 다시 재입사 처리 (STATUS를 050001로 변경)
     */
    public int rehireEmp(String employeeId) {
        try {
            EmpVO emp = empMapper.selectEmp(employeeId);
            if (emp == null) {
                throw new RuntimeException("재입사 처리할 사원을 찾을 수 없습니다.");
            }
            
            // 퇴사한 사원인지 확인
            if (STATUS_ACTIVE.equals(emp.getStatus())) {
                throw new RuntimeException("이미 재직 중인 사원입니다.");
            }
            
            // STATUS를 050001로 변경하여 재입사 처리
            emp.setStatus(STATUS_ACTIVE);
            emp.setResignDate(null); // 퇴사일 초기화
            emp.setUpdateDate(new Date());
            
            return empMapper.updateEmp(emp);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 사원이 재직중인지 확인
     */
    public boolean isActiveEmp(String employeeId) {
        EmpVO emp = empMapper.selectEmp(employeeId);
        return emp != null && emp.isActive();
    }
    
    /**
     * 재직중인 모든 사원 조회
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
        // 기본값 반환 (직접 호출되는 경우)
        return "olivin10001";
    }
    
    /**
     * ✅ 회사 유형별 다음 사원 ID 생성 (개선된 버전)
     * @param compId 회사 ID
     * @return 생성된 사원 ID
     */
    public String getNextEmpId(String compId) {
        try {
            // 회사 정보 조회해서 유형 확인
            String compType = null;
            try {
                compType = empMapper.selectCompanyType(compId);
            } catch (Exception e) {
                System.err.println("회사 유형 조회 실패: " + e.getMessage());
            }
            
            String prefix = "olivin";
            int rangeStart, rangeEnd;
            
            // 회사 유형별 범위 설정 (회사 유형을 알 수 없으면 기본 범위 사용)
            if (compType != null) {
                switch (compType) {
                    case "100001": // 본사
                        rangeStart = 10001;
                        rangeEnd = 39999;
                        break;
                    case "100002": // 지점
                        rangeStart = 40001;
                        rangeEnd = 69999;
                        break;
                    case "100003": // 공급업체
                        rangeStart = 70001;
                        rangeEnd = 99999;
                        break;
                    default:
                        System.err.println("알 수 없는 회사 유형, 기본 범위 사용: " + compType);
                        rangeStart = 10001;
                        rangeEnd = 39999;
                        break;
                }
            } else {
                // 회사 유형을 알 수 없으면 기본 범위 사용
                System.err.println("회사 유형을 알 수 없어 기본 범위 사용: " + compId);
                rangeStart = 10001;
                rangeEnd = 39999;
            }
            
            // 해당 범위에서 마지막 사원 ID 조회
            String lastEmpId = null;
            try {
                lastEmpId = empMapper.selectLastEmpIdByRange(rangeStart, rangeEnd);
            } catch (Exception e) {
                System.err.println("마지막 사원 ID 조회 실패: " + e.getMessage());
            }
            
            int nextNum;
            if (lastEmpId != null && !lastEmpId.isEmpty() && lastEmpId.startsWith(prefix)) {
                // 기존 사원이 있는 경우
                try {
                    String numPart = lastEmpId.substring(prefix.length());
                    int lastNum = Integer.parseInt(numPart);
                    nextNum = lastNum + 1;
                    
                    // 범위 초과 확인
                    if (nextNum > rangeEnd) {
                        System.err.println(getCompanyTypeName(compType) + " 사원번호 범위 초과, 처음부터 시작");
                        nextNum = rangeStart;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("사원 ID 숫자 변환 실패: " + lastEmpId);
                    nextNum = rangeStart;
                }
            } else {
                // 첫 번째 사원인 경우
                nextNum = rangeStart;
            }
            
            String newEmpId = prefix + String.format("%05d", nextNum);
            System.out.println("새로운 사원 ID 생성: " + newEmpId + " (회사: " + compId + ", 유형: " + compType + ")");
            
            return newEmpId;
            
        } catch (Exception e) {
            System.err.println("사원 ID 생성 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 모든 실패 시 기본값 반환
            return "olivin10001";
        }
    }
    
    /**
     * 회사 유형명 반환
     */
    private String getCompanyTypeName(String compType) {
        if (compType == null) return "알 수 없음";
        switch (compType) {
            case "100001": return "본사";
            case "100002": return "지점";
            case "100003": return "공급업체";
            default: return "알 수 없음(" + compType + ")";
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
     * 부서에서 사용 중인 재직중 사원이 있는지 확인
     */
    public boolean hasActiveEmpsInDept(String departmentId) {
        return empMapper.countActiveEmpsByDeptId(departmentId) > 0;
    }
    
    /**
     * 사원 사용 여부 종합 확인
     * @param employeeId 사원 ID
     * @return 사용 여부 정보가 담긴 Map
     */
    public Map<String, Object> checkEmpUsage(String employeeId) {
        // 실제로는 발주서, 계정원장 등에서 사원이 사용되고 있는지 확인
        // 현재는 기본 구조만 구현
        return Map.of(
            "isUsed", false,
            "purchaseOrderCount", 0,
            "accountLedgerCount", 0,
            "details", Map.of(
                "hasPurchaseOrders", false,
                "hasAccountLedgers", false
            )
        );
    }
}