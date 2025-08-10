package com.olivin.app.outbnd.mapper;

import java.util.List;

import com.olivin.app.outbnd.service.OutbndVO;

/*
 * made by kms
 * 
 */
public interface OutbndMapper {
    List<OutbndVO> selectAllList(); // 전체 출고 목록 조회
    String selectCodeForUpdate(); // 출고등록을 위한 출고번호 생성

    void callProcHqOutbndProcess(String orderId); // 제품출고용 프로시저 생성
}
