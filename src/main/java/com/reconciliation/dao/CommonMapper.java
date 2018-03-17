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
public interface CommonMapper {

    /**
     * 查询  所有供应商 数据库链接 信息
     * @return
     */
    public List<Map<String,Object>> getPytoolParkClientAll();

    /**
     * 查询  一个供应商 数据库链接 信息
     * @param parkid
     * @return
     */
    public Map<String,Object> getOnePytoolParkClient(String parkid);

    /**
     * 修改数据连接信息
     * @param param
     * @return
     */
    public int updateParkClient(Map<String,String> param);

    /**
     * 停车场编号转换
     * @return
     */
    public Map<String,String> findOnePlatformransforParkId(String optId);

    /**
     * 添加  银联  对账单数据
     * @param param
     * @return
     */
    public int insertUnionpay(Map<String,Object> param);

    /**
     * 查询   银联 的对账信息
     * @param param
     * @return
     */
    public List<Map<String,Object>> getUnionPayUploadData(Map<String,Object> param);

    /**
     * 获取招商代扣获取银联权益的数据是否存在
     * @param pRecordIdList
     * @return
     */
    public List<Map<String,Object>>  getEquityPlatPayOrCmbAutoPayInfo(String pRecordIdList);

    /**
     *  查询连接信息表中的所有的停车场信息
     * @return
     */
    public List<Map<String,Object>> getParkList();

}
