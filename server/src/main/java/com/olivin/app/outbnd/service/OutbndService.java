package com.olivin.app.outbnd.service;

import java.util.List;

public interface OutbndService {
    List<OutbndVO> getAllOutbnd(); // 전체 출고 목록 조회
    String getCodeForUpdate();     // 출고 등록을 위한 출고번호 생성

    void processHqOutbnd(String orderId); // 출고 처리 프로시저 호출용 메서드
}
