package com.olivin.app.standard.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.olivin.app.standard.service.CompanyVO;

@Mapper
public interface CompanyMapper {
    
    // 모든 회사 목록 조회
    List<CompanyVO> selectAllCompanies();
    
    // 조건별 회사 목록 조회
    List<CompanyVO> selectCompanyList(CompanyVO companyVO);
    
    // 검색 조건에 따른 회사 목록 조회
    List<CompanyVO> selectCompaniesByCondition(Map<String, Object> searchParams);
    
    // 특정 회사 조회
    CompanyVO selectCompany(String compId);
    
    // 회사 등록
    int insertCompany(CompanyVO companyVO);
    
    // 회사 정보 수정
    int updateCompany(CompanyVO companyVO);
    
    // 회사 삭제
    int deleteCompany(String compId);
    
    // 회사 ID 존재 여부 확인
    int checkCompanyId(String compId);
    
    // 회사 유형별 조회
    List<CompanyVO> selectCompaniesByType(String compType);
    
    // 마지막 회사 ID 조회 (ID 자동생성용)
    String selectLastCompanyIdByType(String compType);
    
    // 사업자번호 중복 확인
    int checkBizNumber(@Param("bizNumber") String bizNumber, @Param("excludeCompId") String excludeCompId);
    
    // 회사명 중복 확인  
    int checkCompanyName(@Param("compName") String compName, @Param("excludeCompId") String excludeCompId);
    
    // 회사 유형별 통계
    List<Map<String, Object>> selectCompanyStatsByType();
    
    // 최근 등록된 회사 조회
    List<CompanyVO> selectRecentCompanies(int limit);
}