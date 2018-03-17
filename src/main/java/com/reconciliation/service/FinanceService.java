package com.reconciliation.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface FinanceService {

    /**
     * 财务对账单下载
     * @throws Exception
     */
    public void  downFinanceExcel(HttpServletRequest request, HttpServletResponse response);


}
