package com.reconciliation.controller;

import com.reconciliation.service.FinanceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2018/3/13.
 * 财务对账
 */
@Controller
@RequestMapping(value = "/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @RequestMapping(value = "/downFinanceExcel")
    public void downFinanceExcel(HttpServletRequest request, HttpServletResponse response){
        financeService.downFinanceExcel(request,response);
    }

}
