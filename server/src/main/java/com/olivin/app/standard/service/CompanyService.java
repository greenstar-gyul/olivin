package com.olivin.app.standard.service;

import java.util.List;
import java.util.Map;

public interface CompanyService {
    
    // 모든 회사 목록 조회
    List<CompanyVO> getAllCompanies();
    
    // 조건별 회사 목록 조회
    List<CompanyVO> getCompanyList(CompanyVO companyVO);
    
    // 특정 회사 조회
    CompanyVO getCompany(String compId);
    
    // 회사 등록
    int saveCompany(CompanyVO companyVO);
    
    // 회사 정보 수정
    int modifyCompany(CompanyVO companyVO);
    
    // 회사 삭제
    int removeCompany(String compId);
    
    // 검색 조건에 따른 회사 목록 조회
    List<CompanyVO> searchCompanies(Map<String, Object> searchParams);
    
    // 회사 ID 존재 여부 확인
    boolean isCompanyIdExists(String compId);
    
    // 회사 유형별 조회
    List<CompanyVO> getCompaniesByType(String compType);
    
    // 사업자번호 중복 확인
    boolean isBizNumberExists(String bizNumber, String excludeCompId);
    
    // 회사명 중복 확인
    boolean isCompanyNameExists(String compName, String excludeCompId);
    
    // 다음 회사 ID 생성
    String getNextCompanyId(String compType);
    
    // 회사 유형별 통계
    List<Map<String, Object>> getCompanyStatsByType();
    
    // 최근 등록된 회사 목록
    List<CompanyVO> getRecentCompanies(int limit);
}