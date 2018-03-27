package com.reconciliation.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reconciliation.dao.CmbMapper;
import com.reconciliation.dao.CommonMapper;
import com.reconciliation.dao.YlMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.CmbService;
import com.reconciliation.service.YlService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUtil;
import com.reconciliation.util.UrlConnectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class CmbServiceImpl  implements CmbService{

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private YlMapper ylMapper;

    @Autowired
    private CmbMapper cmbMapper;

    @Autowired
    private Config config;


    /**
     *  招商对账信息
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return
     * @throws Exception
     */
    public Map<String,Object> getStatementInfo(String parkId, String beginDate, String endDate) throws Exception{
        Map<String,Object> map = commonMapper.getOnePytoolParkClient(parkId);
        Map<String,Object> ftpData = null;
        List<StatementAccount> statementAccountList = new ArrayList<StatementAccount>();
        boolean error_flag = false;  //如果读取FPT文件失败就读取数据库
        if(map.get("isFpt").toString().split(",")[0].equals("0")){  //去读取ftp上传的txt文件
            String temp_parkId = parkId;
            FileUtil fileUtil = new FileUtil();
            if(map.get("isCloud").equals("1")){  //如果是云平台上传的FPT文件,停车场编号
                temp_parkId = commonMapper.findOnePlatformransforParkId(parkId).get("platform_id");
                ftpData = fileUtil.readFileByLines(config,parkId,map.get("parkNameAbbreviation").toString(), beginDate, endDate,temp_parkId);
            }else{
                ftpData = fileUtil.readFileByLines(config,parkId,map.get("parkNameAbbreviation").toString(), beginDate, endDate,null);
            }
            statementAccountList = (List<StatementAccount>)ftpData.get("list");
            if(statementAccountList==null){
                error_flag = true;
            }else{
                error_flag = false;
            }
        }
        if(error_flag){
            //查询数据库
            String new_beginDate = beginDate.replace(" ", "%20");
            String new_endDate = endDate.replace(" ", "%20");
            String responseMessage = UrlConnectUtil.getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?starttime="+new_beginDate+"&deadline="+new_endDate);
            Map<String,Object> json = (Map<String,Object>) JSONObject.parse(responseMessage);
            List<Map<String,Object>> jsonDetail = (List<Map<String,Object>>)JSONObject.parse(json.get("page_data").toString());
            for(int i = 0;i<jsonDetail.size();i++){
                StatementAccount statementAccount = new StatementAccount();
                statementAccount.setpRecordId(Integer.parseInt(jsonDetail.get(i).get("pid").toString()));
                statementAccount.setParkId(Integer.parseInt(jsonDetail.get(i).get("parkid").toString()));
                statementAccount.setPlateNum(jsonDetail.get(i).get("plateNo").toString());
                statementAccount.setOriginalAmount(Double.parseDouble(jsonDetail.get(i).get("payCharge").toString()));
                statementAccount.setRecordAmount(Double.parseDouble(jsonDetail.get(i).get("realCharge").toString()));
                statementAccount.setCouponamount(Double.parseDouble(jsonDetail.get(i).get("discount").toString()));
                String[] dateArr = DateUtils.getSplitDate(DateUtils.formatStrToDate1(jsonDetail.get(i).get("chargeTime").toString()));
                statementAccount.setTradeDate(dateArr[0]);
                statementAccount.setTradeTime(dateArr[1]);
                statementAccount.setStatus(jsonDetail.get(i).get("state").toString());
                statementAccount.setExplain(jsonDetail.get(i).get("chargeKind").toString());
                statementAccountList.add(statementAccount);
            }
        }

        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId", parkId);
        param.put("beginDate", beginDate);
        param.put("endDate", endDate);
        List<Map<String,Object>> kabinReconciliation  = cmbMapper.getCmbAutoPayInfo(param);   //卡宾支付信息  招商代扣
        param.put("paytrench_paytype","6"); //招商代扣
        List<Map<String,Object>> unionPayUploadData = cmbMapper.getUnionPayUploadData(param); //支付渠道支付信息

        //过滤支付渠道除所选停车场外的的数据
        //根据支付渠道有的数据一咻数据库肯定会有的情况来过滤的   注：招商代扣账单没有parkId
        Iterator<Map<String,Object>> unionPayIter = unionPayUploadData.iterator();
        while (unionPayIter.hasNext()) {
            Map<String,Object>  unionPay = unionPayIter.next();
            boolean flag = false;
            for(int j = 0;j<kabinReconciliation.size();j++){
                Map<String,Object> temp = kabinReconciliation.get(j);
                if(unionPay.get("cmbId").equals(temp.get("dkid"))){
                    flag = true;
                    break;
                }
            }
            if (!flag){
                unionPayIter.remove();
            }
        }

        //排除停车场多余得数据，不用和一咻数据进行对比的数据
        Iterator<StatementAccount> statementIter = statementAccountList.iterator();
        while (statementIter.hasNext()) {
            StatementAccount  statement = statementIter.next();

            boolean flag = false;
            for(int j = 0;j<kabinReconciliation.size();j++){
                Map<String,Object> temp = kabinReconciliation.get(j);
                if(StringUtils.isNotBlank(temp.get("dkid").toString())){
                    if(statement.getpRecordId().equals(temp.get("pRecordId"))){
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag){
                statementIter.remove();
            }
        }

        for(int i = 0;i<kabinReconciliation.size();i++){
            Map<String,Object> temp = kabinReconciliation.get(i);
            if(temp.get("paytype").equals("AutoPay"))  //自动支付
                temp.put("paytypename", "自动支付");
            else if(temp.get("paytype").equals("UnionPay"))  //银联支付
                temp.put("paytypename", "银联支付");
            else if(temp.get("paytype").equals("AliPay"))  //支付宝支付
                temp.put("paytypename", "支付宝支付");
            else if(temp.get("paytype").equals("WechatPay"))  //微信支付
                temp.put("paytypename", "微信支付");
            else if(temp.get("paytype").equals("CouponPay"))  //优惠劵支付
                temp.put("paytypename", "优惠劵支付");
            else if(temp.get("paytype").equals("UnionAutoPay"))  //银联代扣支付
                temp.put("paytypename", "银联代扣支付");
            else if(temp.get("paytype").equals("RePay"))  //补缴
                temp.put("paytypename", "补缴");
            else if(temp.get("paytype").equals("PrePay"))  //预缴费
                temp.put("paytypename", "预缴费");
            else if(temp.get("paytype").equals("AliRePay"))  //支付宝补缴
                temp.put("paytypename", "支付宝补缴");
            else if(temp.get("paytype").equals("EquityPlatPay"))  //权益平台支付
                temp.put("paytypename", "权益平台支付");
            else if(temp.get("paytype").equals("YiXiuWxPay"))  //一咻微信自助缴费
                temp.put("paytypename", "一咻微信自助缴费");
            else if(temp.get("paytype").equals("czPay"))  //浙商支付
                temp.put("paytypename", "浙商支付");
            else if(temp.get("paytype").equals("sshPay"))  //上海银联支付
                temp.put("paytypename", "上海银联支付");
            else if(temp.get("paytype").equals("CmbPay"))  //招商主动支付
                temp.put("paytypename", "招商主动支付");
            else if(temp.get("paytype").equals("CmbAutoPay"))  //招商代扣
                temp.put("paytypename", "招商代扣");
            else if(temp.get("paytype").equals("chinaumsPaySMWxpay-out"))  //银联商务支付扫码微信
                temp.put("paytypename", "银联商务支付扫码微信");
            else if(temp.get("paytype").equals("chinaumsPaySMAlipay-out"))  //浙银联商务支付支付宝支付
                temp.put("paytypename", "银联商务支付支付宝支付");
            else if(temp.get("paytype").equals("chinaumsPaySMWxpay-in"))  //银联商务支付扫码微信
                temp.put("paytypename", "银联商务支付扫码微信");
            else if(temp.get("paytype").equals("chinaumsPaySMAlipay-in"))  //银联商务支付支付宝支付
                temp.put("paytypename", "银联商务支付支付宝支付");
            else if(temp.get("paytype").equals("UnionpayWallet"))  //银联钱包支付
                temp.put("paytypename", "银联钱包支付");
            else if(temp.get("paytype").equals("ApplePay"))
                temp.put("paytypename","ApplePay");
        }

        List<Map<String,Object>> yesAllList = new ArrayList<Map<String,Object>>(); //存放三方都对上的数据
        List<Map<String, Object>> yes = new ArrayList<Map<String,Object>>();  //存放停车场、一咻对账成功的数据
        List<Map<String, Object>> no = new ArrayList<Map<String,Object>>();   //存放停车场、一咻对账pRecordId或plateNum对上，金额没有对上的额数据
        List<Map<String, Object>> cabin_noAll = new ArrayList<Map<String,Object>>();   //存放卡宾对账不成功的数据
        List<Map<String, Object>> park_noAll = new ArrayList<Map<String,Object>>();   //存放停车场对账不成功的数据

        Double yes_all_money = 0d;  //三方对账成功总金额

        Double park_originalAmountAll = 0d;  //停车场账单总应付金额
        Double park_recordAmountAll = 0d;   //停车场账单总实付金额
        Double park_couponamountAll = 0d;  //听停车场账单总优惠金额

        Double cabin_originalAmountAll = 0d;  //卡宾账单总应付金额
        Double cabin_recordAmountAll = 0d;   //卡宾账单总实付金额
        Double cabin_couponamountAll = 0d;  //卡宾账单总优惠金额

        Double yes_originalAmountAll = 0d;  //对账成功得总应付金额
        Double yes_recordAmountAll = 0d;    //对账成功得总实付金额
        Double yes_couponamountAll = 0d;    //对账成功得总优惠金额

        Double no_originalAmountAll = 0d;  //对账不完全成功得总应付金额     pRecordId或plateNum对应上，金额没对应上
        Double no_recordAmountAll = 0d;    //对账不完全成功得总实付金额     pRecordId或plateNum对应上，金额没对应上
        Double no_couponamountAll = 0d;    //对账不完全成功得总优惠金额     pRecordId或plateNum对应上，金额没对应上

        Double park_noAll_originalAmountAll = 0d;  //停车场对账失败得总应付金额
        Double park_noAll_recordAmountAll = 0d;    //停车场对账失败得总实付金额
        Double park_noAll_couponamountAll = 0d;    //停车场对账失败得总优惠金额

        Double cabin_noAll_originalAmountAll = 0d;  //卡宾对账失败得总应付金额
        Double cabin_noAll_recordAmountAll = 0d;    //卡宾对账失败得总实付金额
        Double cabin_noAll_couponamountAll = 0d;    //卡宾对账失败得总优惠金额


       /* List<StatementAccount> park_reconciliation_list = new ArrayList<StatementAccount>();  //过滤除招商代扣的所有数据 按照停车场有的数据一咻肯定有数据的情况来过滤
        for(int i = 0;i<kabinReconciliation.size();i++){
            Map<String,Object> temp = kabinReconciliation.get(i);
            for(int j = 0;j<statementAccountList.size();j++){
                StatementAccount statementAccount= statementAccountList.get(j);
                if(temp.get("pRecordId").equals(statementAccount.getpRecordId())){
                    park_reconciliation_list.add(statementAccount);
                    break;
                }
            }
        }*/

        int filter_count = 0;
        //以停车场为中心进行对账
        for(int i = 0;i<statementAccountList.size();i++){
            StatementAccount statementAccount = statementAccountList.get(i);
            Date datetime = DateUtils.formatStrToDate(statementAccount.getTradeDate()+statementAccount.getTradeTime());//停车场FTP文件日期
            if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(beginDate).getTime())==-1){  //该条记录的时间必须比开始时间要大
                filter_count++;
                continue;
            }
            if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(endDate).getTime())==1){  //该条记录的时间必须比结束时间要小
                filter_count++;
                continue;
            }
            park_originalAmountAll += statementAccount.getOriginalAmount();
            park_recordAmountAll += statementAccount.getRecordAmount();
            park_couponamountAll += statementAccount.getCouponamount();
            boolean flag = false;
            for(int j = 0;j<kabinReconciliation.size();j++){
                Map<String,Object> temp = kabinReconciliation.get(j);

                if(temp.get("pRecordId")==null||"".equals(temp.get("pRecordId"))){  //主动支付没有pRecordId
                    if(temp.get("plateNum").equals(statementAccount.getPlateNum())){
                        String cabintime = temp.get("createtime").toString();
                        String parktime = statementAccount.getTradeDate()+statementAccount.getTradeTime();
                        parktime = DateUtils.convertDateFormat(parktime);
                        long cha = DateUtils.getDistanceTimes(cabintime, parktime);  //60秒内车牌号对应上ok
                        if(cha>60){
                            break;
                        }
                        flag = true;
                        double originalAmount = 0;
                        if(temp.get("originalAmount")!=null&&!temp.get("originalAmount").equals("")){
                            originalAmount = Double.parseDouble(temp.get("originalAmount").toString());  //卡宾应付金额
                        }
                        double recordAmount = 0;
                        if(temp.get("recordAmount")!=null&&!temp.get("recordAmount").equals("")){
                            recordAmount = Double.parseDouble(temp.get("recordAmount").toString());    //卡宾实付金额
                        }
                        double couponamount = 0;
                        if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                            couponamount = Double.parseDouble(temp.get("couponamount").toString());    //卡宾优惠金额
                        }
                        double originalAmount1 = statementAccount.getOriginalAmount(); //停车场应付金额
                        double recordAmount1 = statementAccount.getRecordAmount();    //停车场实付金额
                        double couponamount1 = statementAccount.getCouponamount();   //停车场优惠金额
                        Map<String, Object> tempMap = new HashMap<String,Object>();
                        tempMap.put("pRecordId", "");  								 //流水号
                        tempMap.put("parkid", statementAccount.getParkId());   		//停车场编号
                        tempMap.put("plateNum", statementAccount.getPlateNum());    //车牌号
                        tempMap.put("cabin_originalAmount", originalAmount);       //卡宾应付金额
                        tempMap.put("cabin_recordAmount", recordAmount);          //卡宾实付金额
                        tempMap.put("cabin_couponamount", couponamount);         //卡宾优惠金额
                        tempMap.put("cabin_date", cabintime);   				//卡宾记录时间
                        tempMap.put("park_originalAmount", originalAmount1);    //停车场应付金额
                        tempMap.put("park_recordAmount", recordAmount1);       //停车场实付金额
                        tempMap.put("park_couponamount", couponamount1);      //停车场优惠金额
                        tempMap.put("park_date", parktime);   				//停车场记录时间
                        tempMap.put("cabin_paytype", temp.get("paytypename"));   //支付类型
                        tempMap.put("cRecordId", temp.get("cRecordId"));
                        tempMap.put("dkid", "");
                        if(originalAmount==originalAmount1&&recordAmount==recordAmount1&&couponamount==couponamount1){  //对账成功
                            yes_originalAmountAll += originalAmount;
                            yes_recordAmountAll += recordAmount;
                            yes_couponamountAll += couponamount;
                            yes_all_money += recordAmount;
                            yes.add(tempMap);
                            yesAllList.add(tempMap);
                        }else{
                            no_originalAmountAll += originalAmount;
                            no_recordAmountAll += recordAmount;
                            no_couponamountAll += couponamount;
                            no.add(tempMap);
                        }
                        break;
                    }
                }else{
                    if(Integer.parseInt(temp.get("pRecordId").toString())==statementAccount.getpRecordId()){
                        flag = true;
                        double originalAmount = 0;
                        if(temp.get("originalAmount")!=null&&!temp.get("originalAmount").equals("")){
                            originalAmount = Double.parseDouble(temp.get("originalAmount").toString());  //卡宾应付金额
                        }
                        double recordAmount = 0;
                        if(temp.get("recordAmount")!=null&&!temp.get("recordAmount").equals("")){
                            recordAmount = Double.parseDouble(temp.get("recordAmount").toString());    //卡宾实付金额
                        }
                        double couponamount = 0;
                        if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                            couponamount = Double.parseDouble(temp.get("couponamount").toString());    //卡宾优惠金额
                        }
                        double originalAmount1 = statementAccount.getOriginalAmount(); //停车场应付金额
                        double recordAmount1 = statementAccount.getRecordAmount();    //停车场实付金额
                        double couponamount1 = statementAccount.getCouponamount();   //停车场优惠金额
                        Map<String, Object> tempMap = new HashMap<String,Object>();
                        String cabintime = temp.get("createtime").toString();
                        String parktime = statementAccount.getTradeDate()+statementAccount.getTradeTime();
                        parktime = DateUtils.convertDateFormat(parktime);
                        tempMap.put("pRecordId", statementAccount.getpRecordId());  //流水号
                        tempMap.put("parkid", statementAccount.getParkId());   		//停车场编号
                        tempMap.put("plateNum", statementAccount.getPlateNum());  //车牌号
                        tempMap.put("cabin_originalAmount", originalAmount);       //卡宾应付金额
                        tempMap.put("cabin_recordAmount", recordAmount);          //卡宾实付金额
                        tempMap.put("cabin_couponamount", couponamount);         //卡宾优惠金额
                        tempMap.put("cabin_date", cabintime);   				//卡宾记录时间
                        tempMap.put("park_originalAmount", originalAmount1);    //停车场应付金额
                        tempMap.put("park_recordAmount", recordAmount1);       //停车场实付金额
                        tempMap.put("park_couponamount", couponamount1);      //停车场优惠金额
                        tempMap.put("park_date", parktime);   				//停车场记录时间
                        tempMap.put("cabin_paytype", temp.get("paytypename"));   //支付类型
                        tempMap.put("cRecordId", temp.get("cRecordId"));
                        tempMap.put("dkid", temp.get("dkid"));
                        if(originalAmount==originalAmount1&&recordAmount==recordAmount1&&couponamount==couponamount1){  //对账成功
                            yes_originalAmountAll += originalAmount;
                            yes_recordAmountAll += recordAmount;
                            yes_couponamountAll += couponamount;
                            yes_all_money += recordAmount;
                            yes.add(tempMap);
                            yesAllList.add(tempMap);
                        }else{
                            no_originalAmountAll += originalAmount;
                            no_recordAmountAll += recordAmount;
                            no_couponamountAll += couponamount;
                            no.add(tempMap);
                        }
                        break;
                    }
                }
            }
            if(!flag){
                if(!(statementAccount.getExplain().equals("微信支付宝支付")||statementAccount.getExplain().equals("预交费")||statementAccount.getExplain().equals("现金收费")||statementAccount.getExplain().equals("预交费成功")||statementAccount.getExplain().equals("未知")||statementAccount.getExplain().equals("余额支付"))){
                    double originalAmount1 = statementAccount.getOriginalAmount(); //停车场应付金额
                    double recordAmount1 = statementAccount.getRecordAmount();    //停车场实付金额
                    double couponamount1 = statementAccount.getCouponamount();   //停车场优惠金额
                    Map<String, Object> tempMap = new HashMap<String,Object>();
                    tempMap.put("pRecordId", statementAccount.getpRecordId());  //流水号
                    tempMap.put("parkid", statementAccount.getParkId());   		//停车场编号
                    tempMap.put("plateNum", statementAccount.getPlateNum());  //车牌号
                    tempMap.put("park_originalAmount", originalAmount1);    //停车场应付金额
                    tempMap.put("park_recordAmount", recordAmount1);       //停车场实付金额
                    tempMap.put("park_couponamount", couponamount1);      //停车场优惠金额
                    String parktime = statementAccount.getTradeDate()+statementAccount.getTradeTime();
                    parktime = DateUtils.convertDateFormat(parktime);
                    tempMap.put("park_date",  parktime);//停车场记录时间
                    tempMap.put("park_explain", statementAccount.getExplain());      //停车场支付类型
                    park_noAll_originalAmountAll += originalAmount1;
                    park_noAll_recordAmountAll += recordAmount1;
                    park_noAll_couponamountAll += couponamount1;
                    park_noAll.add(tempMap);
                }
            }
        }


        //以一咻为中心记录数据
        for(int i= 0 ;i<kabinReconciliation.size();i++){
            Map<String,Object> temp = kabinReconciliation.get(i);
            cabin_originalAmountAll += Double.parseDouble(temp.get("originalAmount").toString());

            cabin_recordAmountAll += Double.parseDouble(temp.get("recordAmount").toString());

            Double couponamountAll = 0d;
            if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                cabin_couponamountAll += Double.parseDouble(temp.get("couponamount").toString());
            }else{
                cabin_couponamountAll += couponamountAll;
            }
            boolean flag = false;
            //和停车场对账
            for(int j = 0;j<statementAccountList.size();j++){
                StatementAccount statementAccount = statementAccountList.get(j);
                Date datetime = DateUtils.formatStrToDate(statementAccount.getTradeDate()+statementAccount.getTradeTime());
                if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(beginDate).getTime())==-1){  //该条记录的时间必须比开始时间要大
                    continue;
                }
                if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(endDate).getTime())==1){  //该条记录的时间必须比结束时间要小
                    continue;
                }
                if(temp.get("pRecordId")==null||temp.get("pRecordId").equals("")){  //主动支付没有pRecordId
                    if(temp.get("plateNum").equals(statementAccount.getPlateNum())){
                        String cabintime = temp.get("createtime").toString();
                        String parktime = statementAccount.getTradeDate()+statementAccount.getTradeTime();
                        parktime = DateUtils.convertDateFormat(parktime);
                        long cha = DateUtils.getDistanceTimes(cabintime, parktime);  //60秒内车牌号对应上ok
                        if(cha>60){
                            break;
                        }
                        flag = true;
                        double originalAmount = 0;
                        if(temp.get("originalAmount")!=null&&!temp.get("originalAmount").equals("")){
                            originalAmount = Double.parseDouble(temp.get("originalAmount").toString());  //卡宾应付金额
                        }
                        double recordAmount = 0;
                        if(temp.get("recordAmount")!=null&&!temp.get("recordAmount").equals("")){
                            recordAmount = Double.parseDouble(temp.get("recordAmount").toString());    //卡宾实付金额
                        }
                        double couponamount = 0;
                        if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                            couponamount = Double.parseDouble(temp.get("couponamount").toString());    //卡宾优惠金额
                        }
                        double originalAmount1 = statementAccount.getOriginalAmount(); //停车场应付金额
                        double recordAmount1 = statementAccount.getRecordAmount();    //停车场实付金额
                        double couponamount1 = statementAccount.getCouponamount();   //停车场优惠金额
                        Map<String, Object> tempMap = new HashMap<String,Object>();
                        tempMap.put("pRecordId", "");  								 //流水号
                        tempMap.put("parkid", statementAccount.getParkId());   		//停车场编号
                        tempMap.put("plateNum", statementAccount.getPlateNum());    //车牌号
                        tempMap.put("cabin_originalAmount", originalAmount);       //卡宾应付金额
                        tempMap.put("cabin_recordAmount", recordAmount);          //卡宾实付金额
                        tempMap.put("cabin_couponamount", couponamount);         //卡宾优惠金额
                        tempMap.put("cabin_date", cabintime);   				//卡宾记录时间
                        tempMap.put("park_originalAmount", originalAmount1);    //停车场应付金额
                        tempMap.put("park_recordAmount", recordAmount1);       //停车场实付金额
                        tempMap.put("park_couponamount", couponamount1);      //停车场优惠金额
                        tempMap.put("park_date", parktime);   				//停车场记录时间
                        tempMap.put("cabin_paytype", temp.get("paytypename"));   //支付类型
                        tempMap.put("cRecordId", temp.get("cRecordId"));
                        tempMap.put("dkid", "");
                        if(originalAmount==originalAmount1&&recordAmount==recordAmount1&&couponamount==couponamount1){  //对账成功
                        }else{
                        }
                        break;
                    }
                }else{
                    if(Integer.parseInt(temp.get("pRecordId").toString())==statementAccount.getpRecordId()){
                        flag = true;
                        double originalAmount = 0;
                        if(temp.get("originalAmount")!=null&&!temp.get("originalAmount").equals("")){
                            originalAmount = Double.parseDouble(temp.get("originalAmount").toString());  //卡宾应付金额
                        }
                        double recordAmount = 0;
                        if(temp.get("recordAmount")!=null&&!temp.get("recordAmount").equals("")){
                            recordAmount = Double.parseDouble(temp.get("recordAmount").toString());    //卡宾实付金额
                        }
                        double couponamount = 0;
                        if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                            couponamount = Double.parseDouble(temp.get("couponamount").toString());    //卡宾优惠金额
                        }
                        double originalAmount1 = statementAccount.getOriginalAmount(); //停车场应付金额
                        double recordAmount1 = statementAccount.getRecordAmount();    //停车场实付金额
                        double couponamount1 = statementAccount.getCouponamount();   //停车场优惠金额
                        Map<String, Object> tempMap = new HashMap<String,Object>();
                        String cabintime = temp.get("createtime").toString();
                        String parktime = statementAccount.getTradeDate()+statementAccount.getTradeTime();
                        parktime = DateUtils.convertDateFormat(parktime);
                        tempMap.put("pRecordId", statementAccount.getpRecordId());  //流水号
                        tempMap.put("parkid", statementAccount.getParkId());   		//停车场编号
                        tempMap.put("plateNum", statementAccount.getPlateNum());  //车牌号
                        tempMap.put("cabin_originalAmount", originalAmount);       //卡宾应付金额
                        tempMap.put("cabin_recordAmount", recordAmount);          //卡宾实付金额
                        tempMap.put("cabin_couponamount", couponamount);         //卡宾优惠金额
                        tempMap.put("cabin_date", cabintime);   				//卡宾记录时间
                        tempMap.put("park_originalAmount", originalAmount1);    //停车场应付金额
                        tempMap.put("park_recordAmount", recordAmount1);       //停车场实付金额
                        tempMap.put("park_couponamount", couponamount1);      //停车场优惠金额
                        tempMap.put("park_date", parktime);   				//停车场记录时间
                        tempMap.put("cabin_paytype", temp.get("paytypename"));   //支付类型
                        tempMap.put("cRecordId", temp.get("cRecordId"));
                        tempMap.put("dkid", temp.get("dkid"));
                        if(originalAmount==originalAmount1&&recordAmount==recordAmount1&&couponamount==couponamount1){  //对账成功
                        }else{
                        }
                        break;
                    }
                }
            }

            if(!flag){
                double originalAmount = Double.parseDouble(temp.get("originalAmount").toString());
                double recordAmount = 0;
                if(temp.get("recordAmount")!=null&&!temp.get("recordAmount").equals("")){
                    recordAmount = Double.parseDouble(temp.get("recordAmount").toString());
                }
                double couponamount = 0;
                if(temp.get("couponamount")!=null&&!temp.get("couponamount").equals("")){
                    couponamount = Double.parseDouble(temp.get("couponamount").toString());
                }
                Map<String, Object> tempMap = new HashMap<String,Object>();
                tempMap.put("cRecordId", temp.get("cRecordId"));
                tempMap.put("pRecordId", temp.get("pRecordId"));  //流水号
                tempMap.put("parkid", temp.get("parkid"));   	//停车场编号
                tempMap.put("plateNum", temp.get("plateNum"));  //车牌号
                tempMap.put("dkid", temp.get("dkid"));  //代扣ID
                tempMap.put("cabin_originalAmount", originalAmount);    //卡宾应付金额
                tempMap.put("cabin_recordAmount", recordAmount);       //卡宾实付金额
                tempMap.put("cabin_couponamount", couponamount);      //卡宾优惠金额
                tempMap.put("cabin_date", temp.get("createtime").toString()); //卡宾记录时间
                tempMap.put("cabin_explain", temp.get("paytypename"));      //卡宾支付类型
                cabin_noAll_originalAmountAll += originalAmount;
                cabin_noAll_recordAmountAll += recordAmount;
                cabin_noAll_couponamountAll += couponamount;
                cabin_noAll.add(tempMap);
            }
        }

        //停车场和一咻对账结果集合   和  支付渠道  进行对账
        Iterator<Map<String,Object>> iter = yesAllList.iterator();
        while (iter.hasNext()) {
            Map<String,Object> temp = iter.next();
            boolean flag = false;
            for(int j = 0;j<unionPayUploadData.size();j++){
                Map<String,Object> unionPay = unionPayUploadData.get(j);
                if(temp.get("dkid").equals(unionPay.get("cmbId"))){
                    temp.put("zs_money", unionPay.get("money"));
                    temp.put("zs_id",unionPay.get("cmbId"));
                    String paytype = null;
                    if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                        paytype = "银联代收";
                    }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                        paytype = "银联权益";
                    }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                        paytype = "applyPay";
                    }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                        paytype = "银联在线";
                    }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                        paytype = "银联钱包";
                    }else if(unionPay.get("payType").equals("6")){   //招商代扣  cmdId
                        paytype = "招商代扣";
                    }
                    flag = true;
                    temp.put("zs_money", unionPay.get("money"));
                    temp.put("zs_id",unionPay.get("cmbId"));
                    temp.put("payType", paytype);
                    if(Double.parseDouble(unionPay.get("money").toString())==Double.parseDouble(temp.get("cabin_recordAmount").toString())){
                        //账单对上了不保存记录，
                    }else{
                        iter.remove();
                        yes_all_money = yes_all_money - Double.parseDouble(temp.get("cabin_recordAmount").toString());
                    }
                    break;
                }
            }
            if(!flag){
                //支付渠道不存在这条记录
                iter.remove();
                yes_all_money = yes_all_money - Double.parseDouble(temp.get("cabin_recordAmount").toString());
            }
        }

        List<Map<String,Object>> zs_yx_yesList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> zs_yx_noList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> zs_yx_noAllList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> cabin_yl_yx_noAllList = new ArrayList<Map<String,Object>>();

        //以招商代扣数据为中心和一咻对账
        for(int i = 0;i<unionPayUploadData.size();i++){
            Map<String,Object> unionPay = unionPayUploadData.get(i);
            boolean flag = false;
            for(int j = 0;j<kabinReconciliation.size();j++){
                Map<String,Object> temp = kabinReconciliation.get(j);
                if(temp.get("dkid").equals(unionPay.get("cmbId"))){
                    String paytype = null;
                    if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                        paytype = "银联代收";
                    }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                        paytype = "银联权益";
                    }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                        paytype = "applyPay";
                    }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                        paytype = "银联在线";
                    }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                        paytype = "银联钱包";
                    }else if(unionPay.get("payType").equals("6")){  //6.招商代扣 CmdId
                        paytype = "招商代扣";
                    }
                    flag = true;
                    Map<String, Object> tempMap = new HashMap<String,Object>();
                    tempMap.put("dkid", temp.get("dkid"));
                    tempMap.put("cRecordId",temp.get("cRecordId"));
                    tempMap.put("pRecordId", temp.get("pRecordId"));  //流水号
                    tempMap.put("parkid", temp.get("parkid"));   	//停车场编号
                    tempMap.put("plateNum", temp.get("plateNum"));  //车牌号
                    tempMap.put("cabin_originalAmount",  temp.get("originalAmount"));       //卡宾应付金额
                    tempMap.put("cabin_recordAmount",  temp.get("recordAmount"));          //卡宾实付金额
                    tempMap.put("cabin_couponamount", temp.get("couponamount"));         //卡宾优惠金额
                    tempMap.put("cabin_date", temp.get("createtime").toString()); //卡宾记录时间
                    tempMap.put("cabin_paytype", temp.get("paytypename"));      //卡宾支付类型
                    tempMap.put("zs_recordAmount",unionPay.get("money"));      //支付渠道支付金额
                    tempMap.put("zs_explain",paytype);         //支付渠道支付类型
                    if(Double.parseDouble(unionPay.get("money").toString())==Double.parseDouble(temp.get("recordAmount").toString())){
                        zs_yx_yesList.add(tempMap);
                    }else{
                        zs_yx_noList.add(tempMap);
                    }
                    break;
                }
            }
            if(!flag){
                String paytype = null;
                if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                    paytype = "银联代收";
                }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                    paytype = "银联权益";
                }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                    paytype = "applyPay";
                }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                    paytype = "银联在线";
                }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                    paytype = "银联钱包";
                }else if(unionPay.get("payType").equals("6")){  //6.招商代扣 CmdId
                    paytype = "招商代扣";
                }
                Map<String, Object> tempMap = new HashMap<String,Object>();
                tempMap.put("zs_recordAmount",unionPay.get("money"));      //支付渠道支付金额
                tempMap.put("zs_explain",paytype);         //支付渠道支付类型
                tempMap.put("zs_num",unionPay.get("cmbId"));
                zs_yx_noAllList.add(tempMap);
            }
        }

        //以一咻为中心和招商代扣数据进行对账
        for(int i = 0;i<kabinReconciliation.size();i++){
            Map<String,Object> temp = kabinReconciliation.get(i);
            boolean flag = false;
            for(int j = 0;j<unionPayUploadData.size();j++){
                Map<String,Object> unionPay = unionPayUploadData.get(j);
                if(temp.get("dkid").equals(unionPay.get("cmbId"))){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                Map<String, Object> tempMap = new HashMap<String,Object>();
                tempMap.put("dkid", temp.get("dkid"));
                tempMap.put("cRecordId",temp.get("cRecordId"));
                tempMap.put("pRecordId", temp.get("pRecordId"));  //流水号
                tempMap.put("parkid", temp.get("parkid"));   	//停车场编号
                tempMap.put("plateNum", temp.get("plateNum"));  //车牌号
                tempMap.put("cabin_originalAmount",  temp.get("originalAmount"));       //卡宾应付金额
                tempMap.put("cabin_recordAmount",  temp.get("recordAmount"));          //卡宾实付金额
                tempMap.put("cabin_couponamount", temp.get("couponamount"));         //卡宾优惠金额
                tempMap.put("cabin_date", temp.get("createtime").toString()); //卡宾记录时间
                tempMap.put("cabin_paytype", temp.get("paytypename"));      //卡宾支付类型
                cabin_yl_yx_noAllList.add(tempMap);
            }
        }

        Map<String,Object> rtnMap = new HashMap<String,Object>();
        rtnMap.put("yesAllList", yesAllList);
        rtnMap.put("yesList", yes);
        rtnMap.put("noList", no);
        rtnMap.put("cabin_noAllList", cabin_noAll);
        rtnMap.put("park_noAllList", park_noAll);

        rtnMap.put("zs_yx_yesList",zs_yx_yesList);
        rtnMap.put("zs_yx_noList",zs_yx_noList);
        rtnMap.put("zs_yx_noAllList",zs_yx_noAllList);
        rtnMap.put("cabin_yl_yx_noAllList",cabin_yl_yx_noAllList);

        if(parkId.equals("1137")){  //天一广场六号岗
            Map<String,Object> tianyi = ylMapper.getTianYiJiFen(param);
            rtnMap.put("tianyi_originalAmount",tianyi.get("tianyi_originalAmount"));
            rtnMap.put("tianyi_mun",tianyi.get("tianyi_mun"));
        }

        rtnMap.put("yes_all_money",yes_all_money);

        rtnMap.put("park_originalAmountAll", park_originalAmountAll);
        rtnMap.put("park_recordAmountAll", park_recordAmountAll);
        rtnMap.put("park_couponamountAll", park_couponamountAll);

        rtnMap.put("cabin_originalAmountAll", cabin_originalAmountAll);
        rtnMap.put("cabin_recordAmountAll", cabin_recordAmountAll);
        rtnMap.put("cabin_couponamountAll", cabin_couponamountAll);

        rtnMap.put("yes_originalAmountAll", yes_originalAmountAll);
        rtnMap.put("yes_recordAmountAll", yes_recordAmountAll);
        rtnMap.put("yes_couponamountAll", yes_couponamountAll);

        rtnMap.put("no_originalAmountAll", no_originalAmountAll);
        rtnMap.put("no_recordAmountAll", no_recordAmountAll);
        rtnMap.put("no_couponamountAll", no_couponamountAll);

        rtnMap.put("park_noAll_originalAmountAll", park_noAll_originalAmountAll);
        rtnMap.put("park_noAll_recordAmountAll", park_noAll_recordAmountAll);
        rtnMap.put("park_noAll_couponamountAll", park_noAll_couponamountAll);

        rtnMap.put("cabin_noAll_originalAmountAll", cabin_noAll_originalAmountAll);
        rtnMap.put("cabin_noAll_recordAmountAll", cabin_noAll_recordAmountAll);
        rtnMap.put("cabin_noAll_couponamountAll", cabin_noAll_couponamountAll);

        System.out.println("停车场支付数量："+((List<StatementAccount>)ftpData.get("list")).size());
        System.out.println("卡宾支付数量："+kabinReconciliation.size());
        System.out.println("被时间过滤停车场支付数量:"+filter_count);
        System.out.println("过滤后的听停车场支付数量:"+(((List<StatementAccount>)ftpData.get("list")).size()-filter_count));
        System.out.println("----------------------------------------------------");

        System.out.println("三方对账成功:"+yesAllList.size());
        System.out.println("对账成功："+yes.size());
        System.out.println("对账不完全失败："+no.size());
        System.out.println("卡宾对账失败："+cabin_noAll.size());
        System.out.println("停车场对账失败："+park_noAll.size());
        System.out.println("----------------------------------------------------");

        System.out.println("支付渠道对账成功："+zs_yx_yesList.size());
        System.out.println("支付渠道不完全成功："+zs_yx_noList.size());
        System.out.println("支付渠道完全失败："+zs_yx_noAllList.size());
        System.out.println("卡宾-支付渠道对账完全失败："+cabin_yl_yx_noAllList.size());


        return rtnMap;
    }

}
