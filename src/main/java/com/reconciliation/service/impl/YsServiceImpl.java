package com.reconciliation.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reconciliation.dao.CommonMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.Result;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.YsService;
import com.reconciliation.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class YsServiceImpl implements YsService {

    private final static Logger logger = LoggerFactory.getLogger(YsServiceImpl.class);

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
    public Map<String,Object> getParkPayInfo(String parkid, String beginDate, String endDate){
        Map<String,Object> map = new HashMap<String,Object>();
        FileUtil fileUtil = new FileUtil();
        Map<String,Object> parkClient = commonMapper.getOnePytoolParkClient(parkid);
        //if(parkClient.get("isFpt").toString().split(",")[1].equals("1")){//'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
            config.setFtp_filePath(config.getYs_path());
        //}
        List<StatementAccount> list = (List<StatementAccount>)fileUtil.readFileByLines_ys(config,parkid,parkClient.get("parkNameAbbreviation").toString(),beginDate,endDate,null).get("list");
        double originalAmount = 0; //应付金额
        double recordAmount = 0; //实付金额
        double couponamount = 0; //优惠金额
        for(int i = 0;i<list.size();i++){
            StatementAccount statementAccount = list.get(i);
            if(statementAccount.getOrderid().equals("00")){
                statementAccount.setOrderid("");
                statementAccount.setPaytime("");
                statementAccount.setMid("");
            }
            String date = statementAccount.getTradeDate();
            statementAccount.setTradeDate(date.substring(0,date.lastIndexOf(":")+3));
            originalAmount += list.get(i).getOriginalAmount();
            recordAmount += list.get(i).getRecordAmount();
            couponamount += list.get(i).getCouponamount();
        }
        map.put("originalAmount",originalAmount);
        map.put("recordAmount",recordAmount);
        map.put("couponamount",couponamount);
        map.put("list",list);
        return  map;
    }


    /**
     * 停车场详细信息导出
     * @throws Exception
     */
    public void downParkDetailExcel(String parkid, String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
        try{
            Map<String,Object> map = new HashMap<String,Object>();
            FileUtil fileUtil = new FileUtil();
            Map<String,Object> parkClient = commonMapper.getOnePytoolParkClient(parkid);
            if(parkClient.get("isFpt").toString().split(",")[1].equals("1")){//'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
                config.setFtp_filePath(config.getYs_path());
            }
            List<StatementAccount> list = (List<StatementAccount>)fileUtil.readFileByLines_ys(config,parkid,parkClient.get("parkNameAbbreviation").toString(),beginDate,endDate,null).get("list");
            double originalAmount = 0; //应付金额
            double recordAmount = 0; //实付金额
            double couponamount = 0; //优惠金额
            for(int i = 0;i<list.size();i++){
                StatementAccount statementAccount = list.get(i);
                String date = statementAccount.getTradeDate();
                if(statementAccount.getOrderid().equals("00")){
                    statementAccount.setOrderid("");
                    statementAccount.setPaytime("");
                    statementAccount.setMid("");
                }
                statementAccount.setTradeDate(date.substring(0,date.lastIndexOf(":")+3));
                originalAmount += list.get(i).getOriginalAmount();
                recordAmount += list.get(i).getRecordAmount();
                couponamount += list.get(i).getCouponamount();
            }
            map.put("originalAmount",originalAmount);
            map.put("recordAmount",recordAmount);
            map.put("couponamount",couponamount);
            map.put("list",list);
            ObjectExcelRead.getParkReconciliationDetailExcel(request,response,map,beginDate,endDate);
        }catch (Exception e){
            logger.info(e.getMessage());

        }
    }


    /**
     * 所有停车场信息导出
     * @throws Exception
     */
    public void downParkAllExcel(String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
        try{

            FileUtil fileUtil = new FileUtil();
            List<Map<String,Object>> parkClientList = commonMapper.getPytoolParkClientAll();
            List<Map<String,Object>> moenyList = new ArrayList<Map<String,Object>>();
            for(int j = 0;j<parkClientList.size();j++){
                Map<String,Object> parkClient = parkClientList.get(j);
                if(parkClient.get("isSearch").toString().equals("1")){
                    if(parkClient.get("isFpt").toString().split(",")[1].equals("1")){//'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
                        config.setFtp_filePath(config.getYs_path());
                    }
                    String newBeginDate = /*DateUtils.format(DateUtils.addOneDay(new Date(),-1))*/beginDate+" 00:00:00";
                    String newEndDate = /*DateUtils.format(DateUtils.addOneDay(new Date(),-1))*/endDate+" 23:59:59";
                    List<StatementAccount> list = (List<StatementAccount>)fileUtil.readFileByLines1(config,parkClient.get("parkId").toString(),parkClient.get("parkNameAbbreviation").toString(),beginDate,endDate,null).get("list");
                    if(list!=null){
                        double originalAmount = 0; //应付金额
                        double recordAmount = 0; //实付金额
                        double couponamount = 0; //优惠金额
                        Map<String,Object> map = new HashMap<String,Object>();
                        for(int i = 0;i<list.size();i++){
                            StatementAccount statementAccount = list.get(i);
                            String date = statementAccount.getTradeDate();
                            statementAccount.setTradeDate(date.substring(0,date.lastIndexOf(":")+3));
                            originalAmount += list.get(i).getOriginalAmount();
                            recordAmount += list.get(i).getRecordAmount();
                            couponamount += list.get(i).getCouponamount();
                        }
                        map.put("parkName",parkClient.get("parkName"));
                        map.put("payCharge",originalAmount);
                        map.put("realCharge",recordAmount);
                        map.put("discount",couponamount);
                        moenyList.add(map);
                    }else{
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put("parkName",parkClient.get("parkName"));
                        map.put("payCharge",0);
                        map.put("realCharge",0);
                        map.put("discount",0);
                        moenyList.add(map);
                    }

                }
            }
            ObjectExcelRead.getParkReconciliationExcel(request,response,moenyList,beginDate,endDate);
        }catch (Exception e){
            logger.info(e.getMessage());

        }
    }


}
