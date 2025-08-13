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
        try {
            System.out.println("=== 회사 등록 시작 ===");
            System.out.println("입력 데이터: " + companyVO.toString());
            
            // ✅ 데이터 전처리
            companyVO.prepareForSave();
            
            // ✅ 필수 필드 검증
            if (!companyVO.isValidForSave()) {
                throw new RuntimeException("필수 필드가 누락되었습니다. 회사명, 회사유형, 사업자번호, CEO명은 필수입니다.");
            }
            
            // 비활성화 타입으로 등록 시도 방지
            if (TYPE_INACTIVE.equals(companyVO.getCompType())) {
                throw new RuntimeException("비활성화 상태로는 새 회사를 등록할 수 없습니다.");
            }
            
            // ✅ 중복 검증
            if (isBizNumberExists(companyVO.getBizNumber(), null)) {
                throw new RuntimeException("이미 등록된 사업자번호입니다: " + companyVO.getBizNumber());
            }
            
            if (isCompanyNameExists(companyVO.getCompName(), null)) {
                throw new RuntimeException("이미 등록된 회사명입니다: " + companyVO.getCompName());
            }
            
            // ✅ 회사 ID 자동생성
            if (companyVO.getCompId() == null || companyVO.getCompId().trim().isEmpty()) {
                String newCompId = getNextCompanyId(companyVO.getCompType());
                companyVO.setCompId(newCompId);
                System.out.println("자동 생성된 회사 ID: " + newCompId);
            }
            
            // ✅ 등록일 설정
            if (companyVO.getRegDate() == null) {
                companyVO.setRegDate(new Date());
            }
            
            // ✅ 등록자 기본값 설정
            if (companyVO.getRegUser() == null || companyVO.getRegUser().trim().isEmpty()) {
                companyVO.setRegUser("SYSTEM");
            }
            
            System.out.println("최종 저장 데이터: " + companyVO.toString());
            
            // ✅ DB 저장
            int result = companyMapper.insertCompany(companyVO);
            
            System.out.println("저장 결과: " + result);
            System.out.println("=== 회사 등록 완료 ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("회사 등록 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("회사 등록 실패: " + e.getMessage());
        }
    }
    
    @Override
    public int modifyCompany(CompanyVO companyVO) {
        try {
            System.out.println("=== 회사 수정 시작 ===");
            System.out.println("수정 데이터: " + companyVO.toString());
            
            // ✅ 데이터 전처리
            companyVO.prepareForSave();
            
            // ✅ 회사 존재 확인
            CompanyVO existingCompany = companyMapper.selectCompany(companyVO.getCompId());
            if (existingCompany == null) {
                throw new RuntimeException("수정할 회사를 찾을 수 없습니다: " + companyVO.getCompId());
            }
            
            // ✅ 중복 검증 (자기 자신 제외)
            if (companyVO.getBizNumber() != null && 
                isBizNumberExists(companyVO.getBizNumber(), companyVO.getCompId())) {
                throw new RuntimeException("이미 등록된 사업자번호입니다: " + companyVO.getBizNumber());
            }
            
            if (companyVO.getCompName() != null && 
                isCompanyNameExists(companyVO.getCompName(), companyVO.getCompId())) {
                throw new RuntimeException("이미 등록된 회사명입니다: " + companyVO.getCompName());
            }
            
            // ✅ 수정일 설정
            companyVO.setUpdateDate(new Date());
            
            // ✅ 수정자 기본값 설정
            if (companyVO.getUpdateUser() == null || companyVO.getUpdateUser().trim().isEmpty()) {
                companyVO.setUpdateUser("SYSTEM");
            }
            
            System.out.println("최종 수정 데이터: " + companyVO.toString());
            
            int result = companyMapper.updateCompany(companyVO);
            
            System.out.println("수정 결과: " + result);
            System.out.println("=== 회사 수정 완료 ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("회사 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("회사 수정 실패: " + e.getMessage());
        }
    }
    
    @Override
    public int removeCompany(String compId) {
        try {
            System.out.println("=== 회사 삭제 시작: " + compId + " ===");
            
            // 회사 정보 조회
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("삭제할 회사를 찾을 수 없습니다: " + compId);
            }
            
            // 이미 비활성화된 회사인지 확인
            if (TYPE_INACTIVE.equals(company.getCompType())) {
                throw new RuntimeException("이미 비활성화된 회사입니다. 필요시 다시 활성화하거나 완전 삭제를 진행하세요.");
            }
            
            // ✅ 사용 여부 종합 확인
            Map<String, Object> usageInfo = checkCompanyUsage(compId);
            boolean isUsed = (Boolean) usageInfo.get("isUsed");
            
            if (isUsed) {
                StringBuilder message = new StringBuilder();
                message.append(String.format("해당 %s는 다음과 같이 사용 중이어서 삭제할 수 없습니다:\n", 
                                            company.getCompTypeName()));
                
                int purchaseOrderCount = (Integer) usageInfo.get("purchaseOrderCount");
                int employeeCount = (Integer) usageInfo.get("employeeCount");
                int productCount = (Integer) usageInfo.get("productCount");
                
                if (purchaseOrderCount > 0) message.append("• 발주서: ").append(purchaseOrderCount).append("건\n");
                if (employeeCount > 0) message.append("• 직원: ").append(employeeCount).append("명\n");
                if (productCount > 0) message.append("• 제품: ").append(productCount).append("건\n");
                
                message.append("\n상태를 '비활성'으로 변경하거나 관련 데이터를 먼저 처리해주세요.");
                
                throw new RuntimeException(message.toString());
            }
            
            // 사용하지 않는 경우에만 삭제 실행
            int result = companyMapper.deleteCompany(compId);
            
            System.out.println("삭제 결과: " + result);
            System.out.println("=== 회사 삭제 완료 ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("회사 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 회사를 비활성화 처리 (COMP_TYPE을 FFFFFF로 변경)
     */
    public int deactivateCompany(String compId) {
        try {
            System.out.println("=== 회사 비활성화 시작: " + compId + " ===");
            
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("비활성화할 회사를 찾을 수 없습니다: " + compId);
            }
            
            // 이미 비활성화된 회사인지 확인
            if (TYPE_INACTIVE.equals(company.getCompType())) {
                throw new RuntimeException("이미 비활성화된 회사입니다.");
            }
            
            // COMP_TYPE을 FFFFFF로 변경하여 비활성화
            company.setCompType(TYPE_INACTIVE);
            company.setUpdateDate(new Date());
            company.setUpdateUser("SYSTEM");
            
            int result = companyMapper.updateCompany(company);
            
            System.out.println("비활성화 결과: " + result);
            System.out.println("=== 회사 비활성화 완료 ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("회사 비활성화 중 오류 발생: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 회사를 다시 활성화 처리 (COMP_TYPE을 원래 유형으로 복원)
     */
    public int reactivateCompany(String compId, String originalCompType) {
        try {
            System.out.println("=== 회사 재활성화 시작: " + compId + " → " + originalCompType + " ===");
            
            CompanyVO company = companyMapper.selectCompany(compId);
            if (company == null) {
                throw new RuntimeException("활성화할 회사를 찾을 수 없습니다: " + compId);
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
            company.setUpdateUser("SYSTEM");
            
            int result = companyMapper.updateCompany(company);
            
            System.out.println("재활성화 결과: " + result);
            System.out.println("=== 회사 재활성화 완료 ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("회사 재활성화 중 오류 발생: " + e.getMessage());
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
    
    // ✅ 명세서에 맞는 회사 ID 생성 로직으로 수정
    @Override
    public String getNextCompanyId(String compType) {
        try {
            System.out.println("회사 ID 생성 시작 - 유형: " + compType);
            
            // 비활성화 타입인 경우 ID 생성 불가
            if (TYPE_INACTIVE.equals(compType)) {
                throw new IllegalArgumentException("비활성화 타입으로는 새 회사 ID를 생성할 수 없습니다.");
            }
            
            String newId;
            
            switch (compType) {
                case TYPE_HEADQUARTERS: // 본사 (COM10001 고정)
                    // 본사가 이미 존재하는지 확인
                    if (isCompanyIdExists("COM10001")) {
                        throw new RuntimeException("본사는 이미 등록되어 있습니다.");
                    }
                    newId = "COM10001";
                    break;
                    
                case TYPE_BRANCH: // 지점 (COM20001~COM49999)
                    String lastBranchId = companyMapper.selectLastCompanyIdByType(TYPE_BRANCH);
                    if (lastBranchId != null && !lastBranchId.isEmpty()) {
                        try {
                            // COM20001 형식에서 숫자 부분만 추출
                            String numberPart = lastBranchId.substring(3); // "20001"
                            int lastNum = Integer.parseInt(numberPart);
                            int nextNum = lastNum + 1;
                            
                            // 지점 범위 확인 (20001~49999)
                            if (nextNum > 49999) {
                                throw new RuntimeException("지점 ID 범위를 초과했습니다. (최대: COM49999)");
                            }
                            
                            newId = "COM" + String.format("%05d", nextNum);
                        } catch (NumberFormatException e) {
                            System.err.println("기존 지점 ID 파싱 오류: " + lastBranchId);
                            newId = "COM20001"; // 기본값
                        }
                    } else {
                        newId = "COM20001"; // 첫 번째 지점
                    }
                    break;
                    
                case TYPE_SUPPLIER: // 공급업체 (COM50001~COM79999)
                    String lastSupplierId = companyMapper.selectLastCompanyIdByType(TYPE_SUPPLIER);
                    if (lastSupplierId != null && !lastSupplierId.isEmpty()) {
                        try {
                            // COM50001 형식에서 숫자 부분만 추출
                            String numberPart = lastSupplierId.substring(3); // "50001"
                            int lastNum = Integer.parseInt(numberPart);
                            int nextNum = lastNum + 1;
                            
                            // 공급업체 범위 확인 (50001~79999)
                            if (nextNum > 79999) {
                                throw new RuntimeException("공급업체 ID 범위를 초과했습니다. (최대: COM79999)");
                            }
                            
                            newId = "COM" + String.format("%05d", nextNum);
                        } catch (NumberFormatException e) {
                            System.err.println("기존 공급업체 ID 파싱 오류: " + lastSupplierId);
                            newId = "COM50001"; // 기본값
                        }
                    } else {
                        newId = "COM50001"; // 첫 번째 공급업체
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("유효하지 않은 회사 유형입니다: " + compType);
            }
            
            // ✅ 생성된 ID가 이미 존재하는지 재확인
            if (isCompanyIdExists(newId)) {
                throw new RuntimeException("생성된 ID가 이미 존재합니다: " + newId + ". 다시 시도해주세요.");
            }
            
            System.out.println("생성된 회사 ID: " + newId);
            return newId;
            
        } catch (Exception e) {
            System.err.println("회사 ID 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ 오류 시에도 명세서에 맞는 안전한 기본값 반환
            String fallbackId;
            switch (compType) {
                case TYPE_HEADQUARTERS:
                    fallbackId = "COM10001";
                    break;
                case TYPE_BRANCH:
                    fallbackId = "COM20001";
                    break;
                case TYPE_SUPPLIER:
                    fallbackId = "COM50001";
                    break;
                default:
                    fallbackId = "COM99999";
            }
            
            // 기본값도 중복이면 다음 번호로 순차 검색
            if (isCompanyIdExists(fallbackId)) {
                if (TYPE_BRANCH.equals(compType)) {
                    // 지점인 경우 20002부터 순차 검색
                    for (int i = 20002; i <= 49999; i++) {
                        String candidateId = "COM" + String.format("%05d", i);
                        if (!isCompanyIdExists(candidateId)) {
                            fallbackId = candidateId;
                            break;
                        }
                    }
                } else if (TYPE_SUPPLIER.equals(compType)) {
                    // 공급업체인 경우 50002부터 순차 검색
                    for (int i = 50002; i <= 79999; i++) {
                        String candidateId = "COM" + String.format("%05d", i);
                        if (!isCompanyIdExists(candidateId)) {
                            fallbackId = candidateId;
                            break;
                        }
                    }
                }
            }
            
            System.out.println("오류로 인한 대체 ID 사용: " + fallbackId);
            return fallbackId;
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
        try {
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
            
        } catch (Exception e) {
            System.err.println("회사 사용 여부 확인 중 오류: " + e.getMessage());
            // 오류 시 안전하게 사용 중이 아닌 것으로 처리
            return Map.of(
                "isUsed", false,
                "purchaseOrderCount", 0,
                "employeeCount", 0,
                "productCount", 0,
                "salesPlanCount", 0,
                "accountLedgerCount", 0,
                "details", Map.of(
                    "hasPurchaseOrders", false,
                    "hasEmployees", false,
                    "hasProducts", false,
                    "hasSalesPlans", false,
                    "hasAccountLedgers", false
                )
            );
        }
    }
}