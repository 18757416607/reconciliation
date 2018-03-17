package com.reconciliation.service.impl;

import com.reconciliation.dao.HangZhouAoTiMapper;
import com.reconciliation.service.HangZhouAoTiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class HangZhouAoTiServiceImpl implements HangZhouAoTiService {

    @Autowired
    private HangZhouAoTiMapper hangZhouAoTiMapper;


    /**
     * 获取杭州奥体支付信息
     * @return
     */
    public List<Map<String,Object>> getHangZhouAoTiPayInfo(Map<String,Object> param){
        return  hangZhouAoTiMapper.getHangZhouAoTiPayInfo(param);
    }


}
