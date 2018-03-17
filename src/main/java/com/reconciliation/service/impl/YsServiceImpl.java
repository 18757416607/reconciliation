package com.reconciliation.service.impl;

import com.reconciliation.dao.CommonMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.service.YsService;
import com.reconciliation.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class YsServiceImpl implements YsService {


    @Autowired
    private Config config;

    @Autowired
    private CommonMapper commonMapper;


    /**
     *  银商需要一咻提供停车场费用信息
     *   获取停车场费用信息
     * @param parkid
     *          停车场编号
     * @param beginDate
     *          开始时间
     * @param endDate
     *          结束时间
     * @return
     */
    public Map<String,Object> getParkPayInfo(String parkid,String beginDate,String endDate){
        FileUtil fileUtil = new FileUtil();
        Map<String,Object> parkClient = commonMapper.getOnePytoolParkClient(parkid);
        if(parkClient.get("isFpt").toString().split(",")[1].equals("1")){//'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
            config.setFtp_filePath("D://YsFile//");
        }
        return  fileUtil.readFileByLines(config,parkid,parkClient.get("parkNameAbbreviation").toString(),beginDate,endDate,null);
    }

}
