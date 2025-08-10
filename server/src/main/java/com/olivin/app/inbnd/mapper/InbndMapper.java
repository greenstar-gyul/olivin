package com.olivin.app.inbnd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.olivin.app.inbnd.service.InbndProdDVO;
import com.olivin.app.inbnd.service.InbndVO;;

public interface InbndMapper {
    List<InbndVO> selectAllList(); // 전체 입고 목록 조회
    String selectCodeForUpdate(); // 출고등록을 위한 출고번호 생성

    List<InbndProdDVO> selectProdLot();
    List<InbndProdDVO> test(@Param("productId") String productId);

    void callProcBrInbndProcess(String orderId); // 제품입고용 프로시저 생성
}
