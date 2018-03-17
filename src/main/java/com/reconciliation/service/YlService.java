package com.reconciliation.service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
public interface YlService {


    /**
     * 获取 各个供应商数据库中  支付表的数据  或  读取ftp上传的txt文件
     * @param parkId  停车场id
     * @return
     */
    public Map<String,Object> getStatementInfo(String parkId/*,String supplierId*/,String beginTime,String endTime) throws Exception;

    /**
     * 停车场对不上的数据，分析对不上的原因
     * @param park_noAll
     * @param param
     * @return
     */
    public List<Map<String,Object>> getParkNoAllRemark(List<Map<String,Object>> park_noAll, Map<String,Object> param);

}
