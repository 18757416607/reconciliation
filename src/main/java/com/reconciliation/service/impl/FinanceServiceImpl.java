package com.reconciliation.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reconciliation.dao.CommonMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.FinanceService;
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
public class FinanceServiceImpl implements FinanceService{

    @Autowired
    private CommonMapper commonMapper;
    @Autowired
    private Config config;

    private final static Logger logger = LoggerFactory.getLogger(FinanceServiceImpl.class);

    /**
     * 财务对账单下载
     * @throws Exception
     */
    public void  downFinanceExcel(HttpServletRequest request, HttpServletResponse response){
        try{
            List<Map<String,Object>> pytoolParkClient = commonMapper.getPytoolParkClientAll();
            logger.info("查询  所有供应商 数据库链接 信息 总条数："+pytoolParkClient.size());
            List<Map<String,Object>> moenyList = new ArrayList<Map<String,Object>>();  //存放金额信息,往excel写
            for(int i = 0;i<pytoolParkClient.size();i++){
                try{
                    Map<String,Object> temp  = pytoolParkClient.get(i);
                    if(temp.get("isSearch").equals("1")){ //'停车场是否可读取数据库或FTP   0:否 1:是
                        if(temp.get("isFpt").toString().split(",")[1].equals("0")){  //'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
                            String temp_parkId = temp.get("parkId").toString();
                            String beginDate = DateUtils.format(DateUtils.addOneDay(new Date(),-1))+" 00:00:00";
                            String endDate = DateUtils.format(DateUtils.addOneDay(new Date(),-1))+" 23:59:59";
                            List<StatementAccount> statementAccountList = new ArrayList<StatementAccount>();
                            FileUtil fileUtil = new FileUtil();
                            if(temp.get("isCloud").toString().equals("1")){  //是否云平台  0:否  1:是'
                                logger.info("参数:parkId====>"+temp.get("parkId").toString());
                                temp_parkId = commonMapper.findOnePlatformransforParkId(temp.get("parkId").toString()).get("platform_id");
                                logger.info("停车场编号转换:===>"+temp_parkId);
                                statementAccountList  = (List<StatementAccount>) fileUtil.finance(config,temp.get("parkId").toString(),temp.get("parkNameAbbreviation").toString(), beginDate, endDate,temp_parkId).get("list");
                            }else{
                                statementAccountList  = (List<StatementAccount>) fileUtil.finance(config,temp.get("parkId").toString(),temp.get("parkNameAbbreviation").toString(), beginDate, endDate,null).get("list");
                            }
                            Map<String,Object> moneyInfo = new HashMap<String,Object>(); //存放金额信息
                            moneyInfo.put("parkName",temp.get("parkName").toString());
                            if(statementAccountList.size()==0){
                                moneyInfo.put("payCharge","0");
                                moneyInfo.put("realCharge","0");
                                moneyInfo.put("discount","0");
                            }else {
                                Double payCharge = 0d;  //应付金额
                                Double realCharge = 0d;  //实付金额
                                Double discount = 0d;    //优惠金额
                                for (int j = 0;j<statementAccountList.size();j++){
                                    StatementAccount statementAccount = statementAccountList.get(j);
                                    payCharge += statementAccount.getOriginalAmount();
                                    realCharge += statementAccount.getRecordAmount();
                                    discount += statementAccount.getCouponamount();
                                }
                                moneyInfo.put("payCharge",payCharge);
                                moneyInfo.put("realCharge",realCharge);
                                moneyInfo.put("discount",discount);
                            }
                            moenyList.add(moneyInfo);

                        }else{
                            //读取数据库
                            String beginDate = DateUtils.format(DateUtils.addOneDay(new Date(),-1))+" 00:00:00";
                            String endDate = DateUtils.format(DateUtils.addOneDay(new Date(),-1))+" 23:59:59";
                            String new_beginDate = beginDate.replace(" ", "%20");
                            String new_endDate = endDate.replace(" ", "%20");
                            Map<String,Object> moneyInfo = new HashMap<String,Object>(); //存放金额信息
                            Double payCharge = 0d;  //应付金额
                            Double realCharge = 0d;  //实付金额
                            Double discount = 0d;    //优惠金额

                            if(temp.get("parkId").toString().equals("1131")){  //和义大道   只能单独获取代扣或主动缴费(需要分别调用接口)（参数：1.自助缴费 2.代扣成功）
                                String responseMessage = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid="+temp.get("parkId")+"&paytype=自助缴费&starttime="+new_beginDate+"&deadline="+new_endDate);
                                Map<String,Object> json = (Map<String,Object>) JSONObject.parse(responseMessage);
                                List<Map<String,Object>> jsonDetail = (List<Map<String,Object>>)JSONObject.parse(json.get("page_data").toString());
                                String responseMessage2 = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid="+temp.get("parkId")+"&paytype=代扣成功&starttime="+new_beginDate+"&deadline="+new_endDate);
                                Map<String,Object> json2 = (Map<String,Object>) JSONObject.parse(responseMessage);
                                List<Map<String,Object>> jsonDetail2 = (List<Map<String,Object>>)JSONObject.parse(json.get("page_data").toString());

                                for(int j=0;j<jsonDetail.size();j++){
                                    Map<String,Object> tempMap = jsonDetail.get(j);
                                    if(!tempMap.get("payCharge").toString().equals("空")){
                                        payCharge += Double.parseDouble(tempMap.get("payCharge").toString());
                                    }
                                    if(!tempMap.get("realCharge").toString().equals("空")){
                                        realCharge += Double.parseDouble(tempMap.get("realCharge").toString());
                                    }
                                    if(!tempMap.get("discount").toString().equals("空")){
                                        discount += Double.parseDouble(tempMap.get("discount").toString());
                                    }
                                }

                                for(int j=0;j<jsonDetail2.size();j++){
                                    Map<String,Object> tempMap = jsonDetail2.get(j);
                                    if(!tempMap.get("payCharge").toString().equals("空")){
                                        payCharge += Double.parseDouble(tempMap.get("payCharge").toString());
                                    }
                                    if(!tempMap.get("realCharge").toString().equals("空")){
                                        realCharge += Double.parseDouble(tempMap.get("realCharge").toString());
                                    }
                                    if(!tempMap.get("discount").toString().equals("空")){
                                        discount += Double.parseDouble(tempMap.get("discount").toString());
                                    }
                                }
                            }else{
                                String responseMessage = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid="+temp.get("parkId")+"&starttime="+new_beginDate+"&deadline="+new_endDate);
                                Map<String,Object> json = (Map<String,Object>) JSONObject.parse(responseMessage);
                                List<Map<String,Object>> jsonDetail = (List<Map<String,Object>>)JSONObject.parse(json.get("page_data").toString());

                                for(int j=0;j<jsonDetail.size();j++){
                                    Map<String,Object> tempMap = jsonDetail.get(j);
                                    if(!tempMap.get("payCharge").toString().equals("空")){
                                        payCharge += Double.parseDouble(tempMap.get("payCharge").toString());
                                    }
                                    if(!tempMap.get("realCharge").toString().equals("空")){
                                        realCharge += Double.parseDouble(tempMap.get("realCharge").toString());
                                    }
                                    if(!tempMap.get("discount").toString().equals("空")){
                                        discount += Double.parseDouble(tempMap.get("discount").toString());
                                    }
                                }

                            }
                            moneyInfo.put("parkName",temp.get("parkName").toString());
                            BigDecimal bg = new BigDecimal(payCharge);
                            payCharge = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            BigDecimal bg1 = new BigDecimal(realCharge);
                            realCharge = bg1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            BigDecimal bg2 = new BigDecimal(discount);
                            discount = bg2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            if(payCharge==0){
                                moneyInfo.put("payCharge","0");
                            }else{
                                moneyInfo.put("payCharge",payCharge);
                            }
                            if(realCharge==0){
                                moneyInfo.put("realCharge","0");
                            }else{
                                moneyInfo.put("realCharge",realCharge);
                            }
                            if(discount==0){
                                moneyInfo.put("discount","0");
                            }else{
                                moneyInfo.put("discount",discount);
                            }
                            moenyList.add(moneyInfo);
                        }
                    }
                }catch (Exception e){
                    continue;
                }

            }
            ObjectExcelRead.getParkReconciliationExcel(request,response,moenyList);
            ResultUtil.requestSuccess("");
        }catch (Exception e){
            ResultUtil.requestFaild("下载财务对账Excel发生异常,请联系开发人员");
            logger.info(e.getMessage());
        }
    }

}
