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
public interface HangZhouAoTiMapper {

    /**
     *  获取杭州奥体支付信息
     * @return
     */
    public List<Map<String,Object>> getHangZhouAoTiPayInfo(Map<String,Object> param);


}
