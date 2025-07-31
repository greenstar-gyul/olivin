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
 */
public interface HeadInventoryService {
    public List<HeadInventoryVO> selectAllHeadInventory();
}
