package com.olivin.app.common.mapper;

import java.util.List;

import com.olivin.app.common.service.BranchCompanySearchVO;

/**
 * Company Search Mapper 인터페이스 <br>
 * 회사 검색 기능을 위한 Mapper 인터페이스입니다.
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.30
 * 수정이력:
 * 2025.07.30 : 최초 작성
 */
public interface BranchCompanySearchMapper {
    // 회사 목록을 조회하는 메서드
    List<BranchCompanySearchVO> selectAllCompanies();

    // 조건에 맞는 회사 목록을 조회하는 메서드
    List<BranchCompanySearchVO> selectCompanyList(String searchValue);
}
