package com.reconciliation.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
@Repository
@Mapper
public interface YlMapper {

    /**
     * 查询总支付表中数据  银联钱包、applePay、银联在线
     * @param param
     * @return
     */
    public List<Map<String,Object>> getParkrecordInfo(Map<String,Object> param);


    /**
     * 查询 银联代扣对账信息   银联代收对账信息在银联代扣表中
     * @param param
     * @return
     */
    public List<Map<String,Object>> getBindUnionPay(Map<String,Object> param);

    /**
     * 获取停车场不需要对账的数据，进行排除
     * @param param
     * @return
     */
    public List<Integer> excludeNoReconciliation(Map<String,Object> param);

    /**
     * 获取某一天中天一六号岗的天一积分有几笔和多少积分
     * @param param
     * @return
     */
    public Map<String,Object> getTianYiJiFen(Map<String,Object> param);

    /**
     * 查询停车场和一咻不一致的数据   代扣数据
     * @param param
     * @return
     */
    public Map<String,Object> getDisagreeData_dk(Map<String,Object> param);

    /**
     * 查询停车场和一咻不一致的数据   总支付表数据  优惠卷形式
     * @param param
     * @return
     */
    public Map<String,Object> getDisagreeData_zzf_yhj(Map<String,Object> param);

    /**
     * 查询停车场和一咻不一致的数据   总支付表数据  微信支付或支付宝
     * @param param
     * @return
     */
    public Map<String,Object> getDisagreeData_zzf_weixinzhifubao(Map<String,Object> param);

}
