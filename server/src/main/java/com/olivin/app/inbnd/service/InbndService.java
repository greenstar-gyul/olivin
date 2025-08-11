package com.olivin.app.inbnd.service;

import java.util.List;

public interface InbndService {
  List<InbndVO> getAllInbnd(); // 전체 입고 목록 조회
  String getCodeForUpdate();     // 입고 등록을 위한 입고번호 생성

  List<InbndProdDVO> getProdByLot(); 
  List<InbndProdDVO> test(String productId);

    void processBrInbnd(String orderId); // 입고 처리 프로시저 호출용 메서드
    void processHqInbnd(String orderId); // 본사 입고 처리 프로시저 호출용 메서드
}
