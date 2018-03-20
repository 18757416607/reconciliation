package com.reconciliation.service;

import com.reconciliation.pojo.Result;
import com.reconciliation.pojo.StatementAccount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface YsService {

    /**
     * 银商需要一咻提供停车场费用信息
     *   获取停车场费用信息
     * @return
     */
    public Map<String,Object> getParkPayInfo(String parkid, String beginDate, String endDate);

    /**
     * 停车场详细信息导出
     * @param parkid
     * @param beginDate
     * @param endDate
     * @param request
     * @param response
     */
    public void downParkDetailExcel(String parkid, String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response);

    /**
     * 所有停车场信息导出
     * @param request
     * @param response
     */
    public void downParkAllExcel(String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response);

}
