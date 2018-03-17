package com.reconciliation.service;

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
    public Map<String,Object> getParkPayInfo(String parkid,String beginDate,String endDate);


}
