package com.olivin.app.outbnd.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.outbnd.service.OutbndService;
import com.olivin.app.outbnd.service.OutbndVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OutbndController {
    private final OutbndService outbndService;

    /**
     * 출고 리스트 조회
     */
    @GetMapping("/outbnd/hqMgmt")
    public List<OutbndVO> outbndList() {
      return outbndService.getAllOutbnd();
    } // end of outbndList()
    
    /**
     * 출고번호 생성
     */
    @GetMapping("/outbnd/getCode")
    public String outbndCode() {
      return outbndService.getCodeForUpdate();
    } // emd pf pitbmdCpde()

    /**
     * 출고 처리 프로시저 호출
     */
    @PostMapping("/outbnd/hqProcess")
    public void processHqOutbnd(@RequestParam String orderId) {
        outbndService.processHqOutbnd(orderId);
    } // end of processHqOutbnd
    
    /**
     * 공급업체 출고 처리 프로시저 호출
     */
    @PostMapping("/outbnd/subProcess")
    public void processSubOutbnd(@RequestParam String orderId) {
        outbndService.processSubOutbnd(orderId);
    } // end of processSubOutbnd

    /*
     * 
     */
    @PostMapping("/outbnd/checkStock")
    public List<String> validateOrderStock(@RequestParam String orderId) {
        return outbndService.validateOrderStock(orderId);
    } // end of processSubOutbnd
} // end of class
