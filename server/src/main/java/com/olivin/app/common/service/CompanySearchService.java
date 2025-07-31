package com.olivin.app.common.service;

import java.util.List;

/**
 * 회사 검색 서비스 인터페이스<br>
 * 모달창의 검색 영역에서 사용되는 회사 검색 기능 인터페이스입니다.<br>
 * <br>
 * 작성자: 이창현<br>
 * 작성일: 2025.07.30<br>
 * 수정이력:<br>
 * - 2025.07.30 : 최초 작성<br>
 */
public interface CompanySearchService {	
	/**
	 * 회사 타입을 지정하는 enum
	 */
	public enum CompanyType {
		all(""), head("본사"), branch("지점"), supplier("공급업체");
		
		private final String name;
		
		CompanyType(String name) {
      this.name = name;
		}

	  public String getName() {
	      return name;
	  }
	};
	
	//모든 회사 조회
	List<CompanySearchVO> selectAllCompanys();
	
	//회사 타입별 조회
	List<CompanySearchVO> selectAllCompanys(CompanyType comType);
	
	//회사 검색 조회
	List<CompanySearchVO> searchCompanys(CompanyType compType, String searchValue);
}
