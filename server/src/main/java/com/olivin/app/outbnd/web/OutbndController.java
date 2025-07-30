package com.olivin.app.outbnd.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.outbnd.service.OutbndService;
import com.olivin.app.outbnd.service.OutbndVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OutbndController {
    private final OutbndService outbndService;
    
    @GetMapping("/hqOutbndMgmt")
    public List<OutbndVO> outbndList() {
      return outbndService.getAllOutbnd();
    } // end of outbndList()
} // end of class
