package com.reconciliation.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface HangZhouAoTiService {

    /**
     * 获取杭州奥体支付信息
     * @return
     */
    public List<Map<String,Object>> getHangZhouAoTiPayInfo(Map<String,Object> param);
}
