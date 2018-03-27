package com.reconciliation.service;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 * 招商对账接口
 */
public interface CmbService {

    /**
     *  招商对账信息
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return
     * @throws Exception
     */
    public Map<String,Object> getStatementInfo(String parkId, String beginDate, String endDate) throws Exception;

}
