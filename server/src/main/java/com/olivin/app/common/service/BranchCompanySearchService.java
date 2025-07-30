package com.olivin.app.common.service;

import java.util.List;

/**
 * Company Search Service 인터페이스 <br>
 * 회사 검색 기능을 위한 서비스 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
public interface BranchCompanySearchService {
    // 전체 회사 목록을 조회합니다.
    List<BranchCompanySearchVO> getAllCompanies();

    // 조건에 맞는 회사 목록을 조회합니다.
    List<BranchCompanySearchVO> getCompanyList(String searchValue);
}
