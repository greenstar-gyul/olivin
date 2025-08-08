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
        // ✅ 발주서만 확인하고 삭제
        try {
            // 발주서 테이블에서 참조하는지 확인 (발주처 또는 공급처)
            int purchaseOrderCount = companyMapper.countPurchaseOrdersByCompId(compId);
            if (purchaseOrderCount > 0) {
                throw new RuntimeException("해당 회사와 관련된 발주서가 " + purchaseOrderCount + "건 있어서 삭제할 수 없습니다. 먼저 발주서들을 처리해주세요.");
            }
            
            // 발주서 확인이 완료되면 삭제 실행
            return companyMapper.deleteCompany(compId);
            
        } catch (Exception e) {
            // 발주서 관련 오류이거나 기타 DB 오류 전달
            throw new RuntimeException(e.getMessage());
        }
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
}