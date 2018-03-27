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
public interface EquityMapper {

    /**
     * 查询   权益 的对账信息
     * @param param
     * @return
     */
    public List<Map<String,Object>> getUnionPayUploadData(Map<String,Object> param);

    /**
     * 查询 权益支付信息
     * @param param
     * @return
     */
    public List<Map<String,Object>> getEquityPayInfo(Map<String,Object> param);

}
