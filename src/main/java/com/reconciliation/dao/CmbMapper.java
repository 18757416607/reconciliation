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
public interface CmbMapper {

    /**
     *  查询   招商 的对账信息
     * @param param
     * @return
     */
    public List<Map<String,Object>> getUnionPayUploadData(Map<String,Object> param);


    /**
     *    查询 招商代扣对账信息
     *      招商代收对账信息在招商代扣表中
     * @param param
     * @return
     */
    public List<Map<String,Object>> getCmbAutoPayInfo(Map<String,Object> param);

}
