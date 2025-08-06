package com.olivin.app.inbnd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.olivin.app.inbnd.service.InbndProdDVO;
import com.olivin.app.inbnd.service.InbndVO;;

public interface InbndMapper {
    List<InbndVO> selectAllList();
    List<InbndProdDVO> selectProdLot();
    List<InbndProdDVO> test(@Param("productId") String productId);
}
