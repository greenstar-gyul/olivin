package com.olivin.app.inbnd.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olivin.app.inbnd.service.InbndService;
import com.olivin.app.inbnd.service.InbndVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InbndController {
    private final InbndService inbndService;

    @GetMapping("/hqInbndMgmt")
    public List<InbndVO> inbndList() { 
      return inbndService.getAllInbnd();
    }  // end of inbndList()
} // end of class
