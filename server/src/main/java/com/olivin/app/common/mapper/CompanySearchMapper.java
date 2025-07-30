package com.olivin.app.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.olivin.app.common.service.CompanySearchVO;

/**
 * 회사 검색 Mapper 인터페이스 <br>
 * 모달창의 검색 영역에서 사용되는 회사 정보 Mapper입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.30<br>
 * 수정이력:<br>
 * - 2025.07.30 : 최초 작성<br>
 */
public interface CompanySearchMapper {
	//전체 회사 목록조회
  List<CompanySearchVO> selectAllCompanys();
  //회사 종류별 목록조회
  List<CompanySearchVO> selectTypeCompanys(String compType);

  // 조건에 맞는 상품 목록을 조회합니다.
  List<CompanySearchVO> selectCompanyList(@Param("compType") String compType, @Param("searchValue") String searchValue);
}
