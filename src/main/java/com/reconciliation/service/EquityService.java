package com.reconciliation.service;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface EquityService {

    /**
     *  权益对账信息
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return
     * @throws Exception
     */
    public Map<String,Object> getStatementInfo(String parkId, String beginDate, String endDate) throws Exception;

}
