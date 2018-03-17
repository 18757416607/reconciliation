package com.reconciliation.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface CommonService {

    /**
     *  数据从银联上获取对账单信息  保存在一咻数据库
     *  1.银联代收
     *  2.主动支付->  applePay
     *  3.主动支付->  银联在线
     *  4.银联权益
     * @throws Exception
     */
    public void writeUnionPay() throws Exception;

    /**
     *  查询连接信息表中的所有的停车场信息
     * @return
     */
    public List<Map<String,Object>> getParkList();

    /**
     * 添加  银联  对账单数据
     * @param param
     * @return
     */
    public int insertUnionpay(Map<String,Object> param);
}
