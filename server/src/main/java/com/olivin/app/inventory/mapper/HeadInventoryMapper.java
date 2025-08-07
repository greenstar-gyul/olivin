package com.olivin.app.inventory.mapper;

import java.util.List;

import com.olivin.app.inventory.service.HeadInventoryVO;

/**
 * HeadInventoryMapper 인터페이스 <br>
 * 본사 재고 매퍼 인터페이스입니다. <br>
 * 
 * 작성자: 함동의 <br>
 * 작성일: 2025.07.30 <br>
 * 수정이력: <br>
 * - 2025.07.30 : 최초 작성 <br>
 * - 2025.07.31 : 재고 검색 기능 추가 <br>
 * - 2025.08.07 : 로트 정보 조회 기능 추가 <br>
 */
public interface HeadInventoryMapper {
    // 본사 재고 관련 SQL 매핑 메소드를 정의합니다.
    public List<HeadInventoryVO> selectAllHeadInventory();

    // 조건에 맞는 본사 재고를 조회하는 메소드입니다.
    public List<HeadInventoryVO> selectHeadInventoryList(HeadInventoryVO headInventoryVO);

    // 본사 재고의 로트 정보를 조회하는 메소드입니다.
    public List<HeadInventoryVO> selectHeadInventoryLot(String productId);
}
