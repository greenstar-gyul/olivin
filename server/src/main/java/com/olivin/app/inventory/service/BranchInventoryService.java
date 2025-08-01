package com.olivin.app.inventory.service;

import java.util.List;

/**
 * BranchInventoryService 인터페이스 <br>
 * 지점 재고 관리 서비스 인터페이스입니다. 
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.31
 * 수정이력:
 * 2025.07.31 : 최초 작성
 */
public interface BranchInventoryService {
    /**
     * 전체 지점 재고를 조회하는 메소드입니다.
     * 
     * @return 전체 지점 재고 목록
     */
    public List<BranchInventoryVO> selectAllBranchInventory();

    /**
     * 조건에 맞는 지점 재고를 조회하는 메소드입니다.
     * 
     * @param branchInventoryVO 검색 조건을 담은 VO 객체
     * @return 조건에 맞는 지점 재고 목록
     */
    public List<BranchInventoryVO> selectBranchInventoryList(BranchInventoryVO branchInventoryVO);
}
