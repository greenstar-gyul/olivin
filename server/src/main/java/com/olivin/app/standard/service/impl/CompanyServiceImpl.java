package com.olivin.app.standard.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olivin.app.standard.mapper.CompanyMapper;
import com.olivin.app.standard.service.CompanyService;
import com.olivin.app.standard.service.CompanyVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {
    
    private final CompanyMapper companyMapper;
    
    // 회사 유형 코드 상수 (공통코드 테이블 기준)
    private static final String TYPE_HEADQUARTERS = "100001";  // 본사
    private static final String TYPE_BRANCH = "100002";        // 지점
    private static final String TYPE_SUPPLIER = "100003";      // 공급업체
    private static final String TYPE_INACTIVE = "FFFFFF";      // 비활성화 (모든 유형 공통)

    @Override
    public List<CompanyVO> getAllCompanies() {
        return companyMapper.selectAllCompanies();
    }
    
    @Override
    public List<CompanyVO> getCompanyList(CompanyVO companyVO) {
        return companyMapper.selectCompanyList(companyVO);
    }
    
    @Override
    public CompanyVO getCompany(String compId) {
        return companyMapper.selectCompany(compId);
    }
    
    @Override
    public int saveCompany(CompanyVO companyVO) {
        // 비활성화 타입으로 등록 시도 방지
        if (TYPE_INACTIVE.equals(companyVO.getCompType())) {
            throw new RuntimeException("비활성화 상태로는 새 회사를 등록할 수 없습니다.");
        }
        
        // 회사 ID 자동생성
        if (companyVO.getCompId() == null || companyVO.getCompId().isEmpty()) {
            String newCompId = getNextCompanyId(companyVO.getCompType());
            companyVO.setCompId(newCompId);
        }
        
        // 등록일 설정
        companyVO.setRegDate(new Date());
        
        // 등록자가 없으면 기본값 설정
        if (companyVO.getRegUser() == null || companyVO.getRegUser().isEmpty()) {
            companyVO.setRegUser("SYSTEM");
        }
        
        return companyMapper.insertCompany(companyVO);
    }
    
    @Override
    public int modifyCompany(CompanyVO companyVO) {
        companyVO.setUpdateDate(new Date());
        return companyMapper.updateCompany(companyVO);
    }
    
    @Override
    public int removeCompany(String compId) {
        try {
            // 회사 정보 조회
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("삭제할 회사를 찾을 수 없습니다.");
            }
            
            // 이미 비활성화된 회사인지 확인
            if (TYPE_INACTIVE.equals(company.getCompType())) {
                throw new RuntimeException("이미 비활성화된 회사입니다. 필요시 다시 활성화하거나 완전 삭제를 진행하세요.");
            }
            
            // 발주서에서 사용 중인지 확인
            int purchaseOrderCount = companyMapper.countPurchaseOrdersByCompId(compId);
            if (purchaseOrderCount > 0) {
                // 회사 유형에 따라 메시지 다르게 설정
                String companyTypeName = company.getCompTypeName();
                
                throw new RuntimeException(
                    String.format("해당 %s는 발주서 %d건에서 사용 중이어서 삭제할 수 없습니다. " +
                                "상태를 '비활성'으로 변경하거나 발주서를 먼저 처리해주세요.", 
                                companyTypeName, purchaseOrderCount)
                );
            }
            
            // 발주서에서 사용하지 않는 경우에만 삭제 실행
            return companyMapper.deleteCompany(compId);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 회사를 비활성화 처리 (COMP_TYPE을 FFFFFF로 변경)
     */
    public int deactivateCompany(String compId) {
        try {
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("비활성화할 회사를 찾을 수 없습니다.");
            }
            
            // 이미 비활성화된 회사인지 확인
            if (TYPE_INACTIVE.equals(company.getCompType())) {
                throw new RuntimeException("이미 비활성화된 회사입니다.");
            }
            
            // COMP_TYPE을 FFFFFF로 변경하여 비활성화
            company.setCompType(TYPE_INACTIVE);
            company.setUpdateDate(new Date());
            
            return companyMapper.updateCompany(company);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 회사를 다시 활성화 처리 (COMP_TYPE을 원래 유형으로 복원)
     * 주의: 원래 유형을 알 수 없으므로 매개변수로 받아야 함
     */
    public int reactivateCompany(String compId, String originalCompType) {
        try {
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("활성화할 회사를 찾을 수 없습니다.");
            }
            
            // 비활성화된 회사인지 확인
            if (!TYPE_INACTIVE.equals(company.getCompType())) {
                throw new RuntimeException("이미 활성화된 회사입니다.");
            }
            
            // 유효한 회사 유형인지 확인
            if (!isValidActiveCompType(originalCompType)) {
                throw new RuntimeException("유효하지 않은 회사 유형입니다: " + originalCompType);
            }
            
            // COMP_TYPE을 원래 유형으로 복원
            company.setCompType(originalCompType);
            company.setUpdateDate(new Date());
            
            return companyMapper.updateCompany(company);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 유효한 활성 회사 유형인지 확인
     */
    private boolean isValidActiveCompType(String compType) {
        return TYPE_HEADQUARTERS.equals(compType) || 
               TYPE_BRANCH.equals(compType) || 
               TYPE_SUPPLIER.equals(compType);
    }
    
    /**
     * 회사가 활성 상태인지 확인
     */
    public boolean isActiveCompany(String compId) {
        CompanyVO company = companyMapper.selectCompany(compId);
        return company != null && company.isActive();
    }
    
    /**
     * 활성 상태의 회사 유형별 조회
     */
    public List<CompanyVO> getActiveCompaniesByType(String compType) {
        return companyMapper.selectActiveCompaniesByType(compType);
    }
    
    /**
     * 활성 상태의 모든 회사 조회
     */
    public List<CompanyVO> getActiveCompanies() {
        return companyMapper.selectActiveCompanies();
    }
    
    @Override
    public List<CompanyVO> searchCompanies(Map<String, Object> searchParams) {
        return companyMapper.selectCompaniesByCondition(searchParams);
    }
    
    @Override
    public boolean isCompanyIdExists(String compId) {
        return companyMapper.checkCompanyId(compId) > 0;
    }
    
    @Override
    public List<CompanyVO> getCompaniesByType(String compType) {
        return companyMapper.selectCompaniesByType(compType);
    }
    
    @Override
    public boolean isBizNumberExists(String bizNumber, String excludeCompId) {
        return companyMapper.checkBizNumber(bizNumber, excludeCompId) > 0;
    }
    
    @Override
    public boolean isCompanyNameExists(String compName, String excludeCompId) {
        return companyMapper.checkCompanyName(compName, excludeCompId) > 0;
    }
    
    @Override
    public String getNextCompanyId(String compType) {
        try {
            // 비활성화 타입인 경우 ID 생성 불가
            if (TYPE_INACTIVE.equals(compType)) {
                throw new IllegalArgumentException("비활성화 타입으로는 새 회사 ID를 생성할 수 없습니다.");
            }
            
            switch (compType) {
                case TYPE_HEADQUARTERS: // 본사
                    return "COM10001";
                    
                case TYPE_BRANCH: // 지점 (20001~49999)
                    String lastBranchId = companyMapper.selectLastCompanyIdByType(TYPE_BRANCH);
                    if (lastBranchId != null && !lastBranchId.isEmpty()) {
                        int lastNum = Integer.parseInt(lastBranchId.substring(3));
                        int nextNum = lastNum + 1;
                        if (nextNum > 49999) {
                            throw new RuntimeException("지점 ID 범위를 초과했습니다.");
                        }
                        return "COM" + String.format("%05d", nextNum);
                    } else {
                        return "COM20001";
                    }
                    
                case TYPE_SUPPLIER: // 공급업체 (50001~79999)
                    String lastSupplierId = companyMapper.selectLastCompanyIdByType(TYPE_SUPPLIER);
                    if (lastSupplierId != null && !lastSupplierId.isEmpty()) {
                        int lastNum = Integer.parseInt(lastSupplierId.substring(3));
                        int nextNum = lastNum + 1;
                        if (nextNum > 79999) {
                            throw new RuntimeException("공급업체 ID 범위를 초과했습니다.");
                        }
                        return "COM" + String.format("%05d", nextNum);
                    } else {
                        return "COM50001";
                    }
                    
                default:
                    throw new IllegalArgumentException("유효하지 않은 회사 유형입니다: " + compType);
            }
            
        } catch (Exception e) {
            System.err.println("회사 ID 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 기본값 반환
            switch (compType) {
                case TYPE_HEADQUARTERS:
                    return "COM10001";
                case TYPE_BRANCH:
                    return "COM20001";
                case TYPE_SUPPLIER:
                    return "COM50001";
                default:
                    return "COM99999";
            }
        }
    }
    
    @Override
    public List<Map<String, Object>> getCompanyStatsByType() {
        return companyMapper.selectCompanyStatsByType();
    }
    
    @Override
    public List<CompanyVO> getRecentCompanies(int limit) {
        return companyMapper.selectRecentCompanies(limit);
    }
    
    /**
     * 회사 사용 여부 종합 확인
     * @param compId 회사 ID
     * @return 사용 여부 정보가 담긴 Map
     */
    public Map<String, Object> checkCompanyUsage(String compId) {
        int purchaseOrderCount = companyMapper.countPurchaseOrdersByCompId(compId);
        int employeeCount = companyMapper.countEmployeesByCompId(compId);
        int productCount = companyMapper.countProductsByCompId(compId);
        int salesPlanCount = companyMapper.countSalesPlansByCompId(compId);
        int accountLedgerCount = companyMapper.countAccountLedgersByCompId(compId);
        
        boolean isUsed = purchaseOrderCount > 0 || employeeCount > 0 || 
                        productCount > 0 || salesPlanCount > 0 || accountLedgerCount > 0;
        
        return Map.of(
            "isUsed", isUsed,
            "purchaseOrderCount", purchaseOrderCount,
            "employeeCount", employeeCount,
            "productCount", productCount,
            "salesPlanCount", salesPlanCount,
            "accountLedgerCount", accountLedgerCount,
            "details", Map.of(
                "hasPurchaseOrders", purchaseOrderCount > 0,
                "hasEmployees", employeeCount > 0,
                "hasProducts", productCount > 0,
                "hasSalesPlans", salesPlanCount > 0,
                "hasAccountLedgers", accountLedgerCount > 0
            )
        );
    }
}