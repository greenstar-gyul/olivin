package com.olivin.app.inventory.mapper;

import java.util.List;

import com.olivin.app.inventory.service.BranchInventoryVO;

/**
 * BranchInventoryMapper 인터페이스 <br>
 * 지점 재고 매퍼 인터페이스입니다. <br>
 * 
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.31 <br>
 * 수정이력: <br>
 * - 2025.07.31 : 최초 작성 <br>
 */
public interface BranchInventoryMapper {
    // 전체 지점 재고를 조회하는 메소드입니다.
    public List<BranchInventoryVO> selectAllBranchInventory();
    /**
     * 조건에 맞는 지점 재고를 조회하는 메소드입니다.
     * 
     * @param branchInventoryVO 검색 조건을 담은 VO 객체
     * @return 조건에 맞는 지점 재고 목록
     */
    public List<BranchInventoryVO> selectBranchInventoryList(BranchInventoryVO branchInventoryVO);
}
