package com.reconciliation.task;

import com.alibaba.fastjson.JSONObject;
import com.reconciliation.dao.CommonMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUtil;
import com.reconciliation.util.UrlConnectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2018/3/15.
 * 针对银商的定时任务
 *  1.指定时间调用一次停车场费用接口写到服务器
 *  2.提供给银商使用
 */
@Component
public class YsJob {

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private Config config;

    private final static Logger logger = LoggerFactory.getLogger(YsJob.class);


    /*public final static long ONE_Minute =  60 * 1000;

    @Scheduled(fixedDelay=ONE_Minute)
    public void fixedDelayJob(){
        System.out.println(DateUtils.formatYYYYMMDDHHMMSS()+" >>fixedDelay执行....");
    }

    @Scheduled(fixedRate=ONE_Minute)
    public void fixedRateJob(){
        System.out.println(DateUtils.formatYYYYMMDDHHMMSS()+" >>fixedRate执行....");
    }*/

    /**
     * 第一位，表示秒，取值0-59
     * 第二位，表示分，取值0-59
     * 第三位，表示小时，取值0-23
     * 第四位，日期天/日，取值1-31
     * 第五位，日期月份，取值1-12
     * 第六位，星期，取值1-7，星期一，星期二...，注：不是第1周，第二周的意思
        另外：1表示星期天，2表示星期一。
     * 第7为，年份，可以留空，取值1970-2099

     (*)星号：可以理解为每的意思，每秒，每分，每天，每月，每年...
     (?)问号：问号只能出现在日期和星期这两个位置，表示这个位置的值不确定，每天3点执行，所以第六位星期的位置，我们是不需要关注的，就是不确定的值。
        同时：日期和星期是两个相互排斥的元素，通过问号来表明不指定值。比如，1月10日，比如是星期1，如果在星期的位置是另指定星期二，就前后冲突矛盾了。
     (-)减号：表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12
     (,)逗号：表达一个列表值，如在星期字段中使用“1,2,4”，则表示星期一，星期二，星期四
     (/)斜杠：如：x/y，x是开始值，y是步长，比如在第一位（秒） 0/15就是，从0秒开始，每15秒，最后就是0，15，30，45，60    另：y，等同于0/y

     例子：
     0 0 3 * * ?     每天3点执行
     0 5 3 * * ?     每天3点5分执行
     0 5 3 ? * *     每天3点5分执行，与上面作用相同
     0 5/10 3 * * ?  每天3点的 5分，15分，25分，35分，45分，55分这几个时间点执行
     0 10 3 ? * 1    每周星期天，3点10分 执行，注：1表示星期天
     0 10 3 ? * 1#3  每个月的第三个星期，星期天 执行，#号只能出现在星期的位置
     */
    @Scheduled(cron="0 04 0 * * ?")
    public void ysJob() throws  Exception{
        //读取数据库连接信息
        logger.info(DateUtils.formatYYYYMMDDHHMMSS() + "==>" + "开始获取停车场费用信息写到服务器上");
        List<Map<String, Object>> pytoolParkClient = commonMapper.getPytoolParkClientAll();
        for (int i = 0; i < pytoolParkClient.size(); i++) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> temp = pytoolParkClient.get(i);
            try {
                if (temp.get("isSearch").equals("1")) { //'停车场是否可读取数据库或FTP   0:否 1:是
                    if (temp.get("isFpt").toString().split(",")[1].equals("0")) {  //'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',

                    } else {
                        FileUtil fileUtil = new FileUtil();
                        //读取数据库
                        String beginDate = DateUtils.format(DateUtils.addOneDay(new Date(), -1)) + " 00:00:00";
                        String endDate = DateUtils.format(DateUtils.addOneDay(new Date(), -1)) + " 23:59:59";
                        String new_beginDate = beginDate.replace(" ", "%20");
                        String new_endDate = endDate.replace(" ", "%20");
                        if (temp.get("parkId").toString().equals("1131")) {  //和义大道   只能单独获取代扣或主动缴费(需要分别调用接口)（参数：1.自助缴费 2.代扣成功）
                            String responseMessage = UrlConnectUtil.getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&paytype=自助缴费&starttime=" + new_beginDate + "&deadline=" + new_endDate);
                        /*String responseMessage = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&paytype=自助缴费&starttime=" + new_beginDate + "&deadline=" + new_endDate);*/
                            Map<String, Object> json = (Map<String, Object>) JSONObject.parse(responseMessage);
                            List<Map<String, Object>> jsonDetail = (List<Map<String, Object>>) JSONObject.parse(json.get("page_data").toString());
                        /*String responseMessage2 = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&paytype=代扣成功&starttime=" + new_beginDate + "&deadline=" + new_endDate);*/
                            String responseMessage2 = UrlConnectUtil.getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&paytype=代扣成功&starttime=" + new_beginDate + "&deadline=" + new_endDate);
                            Map<String, Object> json2 = (Map<String, Object>) JSONObject.parse(responseMessage2);
                            List<Map<String, Object>> jsonDetail2 = (List<Map<String, Object>>) JSONObject.parse(json2.get("page_data").toString());
                            for (int j = 0; j < jsonDetail.size(); j++) {
                                if (jsonDetail.get(j).get("pid") != null && !"".equals(jsonDetail.get(j).get("pid")) && !"空".equals(jsonDetail.get(j).get("pid"))) {
                                    Map<String, String> tempMap = commonMapper.findUnionAutoPayRecordIsExist(jsonDetail.get(j).get("pid").toString(), temp.get("parkId").toString());
                                    if (tempMap != null) {
                                        jsonDetail.get(j).put("orderid", tempMap.get("orderid"));
                                        jsonDetail.get(j).put("paytime", tempMap.get("paytime"));
                                        jsonDetail.get(j).put("mid", tempMap.get("mid"));
                                    } else {
                                        //如果此条记录不是银联代扣的,值为""
                                        jsonDetail.get(j).put("orderid", "00");
                                        jsonDetail.get(j).put("paytime", "00");
                                        jsonDetail.get(j).put("mid", "00");
                                    }
                                } else {
                                    //如果此条记录不是银联代扣的,值为00
                                    jsonDetail.get(j).put("orderid", "00");
                                    jsonDetail.get(j).put("paytime", "00");
                                    jsonDetail.get(j).put("mid", "00");
                                }

                                list.add(jsonDetail.get(j));
                            }
                            for (int j = 0; j < jsonDetail2.size(); j++) {
                                if (jsonDetail2.get(j).get("pid") != null && !"".equals(jsonDetail2.get(j).get("pid")) && !"空".equals(jsonDetail2.get(j).get("pid"))) {
                                    Map<String, String> tempMap = commonMapper.findUnionAutoPayRecordIsExist(jsonDetail2.get(j).get("pid").toString(), temp.get("parkId").toString());
                                    if (tempMap != null) {
                                        jsonDetail2.get(j).put("orderid", tempMap.get("orderid"));
                                        jsonDetail2.get(j).put("paytime", tempMap.get("paytime"));
                                        jsonDetail2.get(j).put("mid", tempMap.get("mid"));
                                    } else {
                                        //如果此条记录不是银联代扣的,值为00
                                        jsonDetail2.get(j).put("orderid", "00");
                                        jsonDetail2.get(j).put("paytime", "00");
                                        jsonDetail2.get(j).put("mid", "00");
                                    }
                                } else {
                                    //如果此条记录不是银联代扣的,值为00
                                    jsonDetail2.get(j).put("orderid", "00");
                                    jsonDetail2.get(j).put("paytime", "00");
                                    jsonDetail2.get(j).put("mid", "00");
                                }
                                list.add(jsonDetail2.get(j));
                            }
                            String date = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(new Date(), -1)));
                            fileUtil.writeToFile(config, list, temp.get("parkId").toString(), temp.get("parkNameAbbreviation").toString(),date);
                            logger.info(DateUtils.formatYYYYMMDDHHMMSS() + "==>" + temp.get("parkId").toString() + "===>" + "数据写入成功!!!");
                        } else {
                        /*String responseMessage = UrlConnectUtil.getHttpJson("http://192.168.1.3:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&starttime=" + new_beginDate + "&deadline=" + new_endDate);*/
                            String responseMessage = UrlConnectUtil.getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?parkid=" + temp.get("parkId") + "&starttime=" + new_beginDate + "&deadline=" + new_endDate);
                            Map<String, Object> json = (Map<String, Object>) JSONObject.parse(responseMessage);
                            List<Map<String, Object>> jsonDetail = (List<Map<String, Object>>) JSONObject.parse(json.get("page_data").toString());
                            for (int j = 0; j < jsonDetail.size(); j++) {
                                if (jsonDetail.get(j).get("pid") != null && !"".equals(jsonDetail.get(j).get("pid")) && !"空".equals(jsonDetail.get(j).get("pid"))) {
                                    Map<String, String> tempMap = commonMapper.findUnionAutoPayRecordIsExist(jsonDetail.get(j).get("pid").toString(), temp.get("parkId").toString());
                                    if (tempMap != null) {
                                        jsonDetail.get(j).put("orderid", tempMap.get("orderid"));
                                        jsonDetail.get(j).put("paytime", tempMap.get("paytime"));
                                        jsonDetail.get(j).put("mid", tempMap.get("mid"));
                                    } else {
                                        //如果此条记录不是银联代扣的,值为00
                                        jsonDetail.get(j).put("orderid", "00");
                                        jsonDetail.get(j).put("paytime", "00");
                                        jsonDetail.get(j).put("mid", "00");
                                    }
                                } else {
                                    //如果此条记录不是银联代扣的,值为00
                                    jsonDetail.get(j).put("orderid", "00");
                                    jsonDetail.get(j).put("paytime", "00");
                                    jsonDetail.get(j).put("mid", "00");
                                }

                                list.add(jsonDetail.get(j));
                            }
                            String date = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(new Date(), -1)));
                            fileUtil.writeToFile(config, list, temp.get("parkId").toString(), temp.get("parkNameAbbreviation").toString(),date);
                            logger.info(DateUtils.formatYYYYMMDDHHMMSS() + "==>" + temp.get("parkId").toString() + "===>" + "数据写入成功!!!");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info(DateUtils.formatYYYYMMDDHHMMSS() + "==>" + temp.get("parkId").toString() + "===>" + "获取接口异常,联系接口编写者");
                continue;
            }
        }
    }

    @Scheduled(cron="0 28 4 * * ?")
    public void ysFptJob() throws  Exception{
        for(int m = 2;m<=79;m++){
        //读取FTP文件
        List<Map<String, Object>> pytoolParkClient = commonMapper.getPytoolParkClientAll();
        for (int i = 0; i < pytoolParkClient.size(); i++) {
            FileUtil fileUtil = new FileUtil();
            Map<String, Object> temp = pytoolParkClient.get(i);
            if (temp.get("isSearch").equals("1")) { //'停车场是否可读取数据库或FTP   0:否 1:是
                if (temp.get("isFpt").toString().split(",")[1].equals("0")) {  //'区分供应商对账数据  0:读取fpt的txt   1:读取数据库',
                    String beginDate = DateUtils.format(DateUtils.addOneDay(new Date(), -m));
                    String endDate = DateUtils.format(DateUtils.addOneDay(new Date(), -m));
                    Map<String, Object> ftpMap = null;
                    try{
                        ftpMap = fileUtil.readFileByLines(config,temp.get("parkId").toString(),temp.get("parkNameAbbreviation").toString(),beginDate,endDate,null);
                        /*Map<String, Object> ftpMap = fileUtil.readFileByLines(config,temp.get("parkId").toString(),temp.get("parkNameAbbreviation").toString(),beginDate,endDate,null);*/
                        List<StatementAccount> statementAccountList = (List<StatementAccount>)ftpMap.get("list");
                        List<Map<String,Object>> writeList = new ArrayList<Map<String,Object>>();
                        for(int j = 0;j<statementAccountList.size();j++){
                            StatementAccount statementAccount =  statementAccountList.get(j);
                            Map<String,Object> writeMap = new HashMap<String,Object>();
                            if (statementAccount.getpRecordId() != null && !"".equals(statementAccount.getpRecordId()) && !"空".equals(statementAccount.getpRecordId())) {
                                Map<String, String> tempMap = commonMapper.findUnionAutoPayRecordIsExist(statementAccount.getpRecordId().toString(), temp.get("parkId").toString());
                                if (tempMap != null) {
                                    writeMap.put("orderid",tempMap.get("orderid"));
                                    writeMap.put("paytime",tempMap.get("paytime"));
                                    writeMap.put("mid",tempMap.get("mid"));
                                }else{
                                    writeMap.put("orderid","00");
                                    writeMap.put("paytime","00");
                                    writeMap.put("mid","00");
                                }
                            }else{
                                writeMap.put("orderid","00");
                                writeMap.put("paytime","00");
                                writeMap.put("mid","00");
                            }
                            writeMap.put("pid",statementAccount.getpRecordId());
                            writeMap.put("parkid",statementAccount.getParkId());
                            writeMap.put("plateNo",statementAccount.getPlateNum());
                            writeMap.put("payCharge",statementAccount.getOriginalAmount());
                            writeMap.put("realCharge",statementAccount.getRecordAmount());
                            writeMap.put("discount",statementAccount.getCouponamount());
                            writeMap.put("chargeTime",DateUtils.convertDateFormat(statementAccount.getTradeDate()+statementAccount.getTradeTime()));
                            writeMap.put("state",statementAccount.getStatus());
                            writeMap.put("chargeKind",statementAccount.getExplain());
                            writeList.add(writeMap);
                        }
                        String date = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(new Date(), -m)));
                        fileUtil.writeToFile(config,writeList,temp.get("parkId").toString(),temp.get("parkNameAbbreviation").toString(),date);
                    }catch (Exception e){
                        continue;
                    }

                }
            }
        }

        }

    }


}
