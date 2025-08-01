package com.olivin.app.inventory.service;

import java.util.List;

/**
 * HeadInventory Service 인터페이스 <br>
 * 본사 재고 관리 서비스 인터페이스입니다. 
 * 
 * 작성자: 함동의
 * 작성일: 2025.07.25
 * 수정이력:
 * 2025.07.25 : 최초 작성
 * 2025.07.30 : 주석 추가, 구현
 * 2025.07.31 : 재고 검색 기능 추가
 */
public interface HeadInventoryService {
    public List<HeadInventoryVO> selectAllHeadInventory();
    /**
     * 조건에 맞는 본사 재고를 조회하는 메소드입니다.
     * 
     * @param headInventoryVO 검색 조건을 담은 VO 객체
     * @return 조건에 맞는 본사 재고 목록
     */
    public List<HeadInventoryVO> selectHeadInventoryList(HeadInventoryVO headInventoryVO);
}
