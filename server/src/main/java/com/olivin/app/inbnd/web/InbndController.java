package com.olivin.app.inbnd.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inbnd.service.InbndProdDVO;
import com.olivin.app.inbnd.service.InbndService;
import com.olivin.app.inbnd.service.InbndVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InbndController {
    private final InbndService inbndService;

    @GetMapping("/inbnd/hqMgmt")
    public List<InbndVO> inbndList() { 
      return inbndService.getAllInbnd();
    }  // end of inbndList()

    @GetMapping("/inbnd/searchByLot")
    public List<InbndProdDVO> prodListByLot() {
      return inbndService.getProdByLot();
    } // end of prodListByLot()

    @GetMapping("/inbnd/test/{productId}")
    public List<InbndProdDVO> test(@PathVariable("productId") String productId) {
      return inbndService.test(productId);
    } // end of prodListByLot()
} // end of class
