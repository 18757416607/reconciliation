package com.reconciliation.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.reconciliation.dao.CommonMapper;
import com.reconciliation.dao.YlMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.YlService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUtil;
import com.reconciliation.util.UrlConnectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class YlServiceImpl implements YlService{

    @Autowired
    private YlMapper ylMapper;
    @Autowired
    private CommonMapper commonMapper;
    @Autowired
    private Config config;
    private final static Logger logger = LoggerFactory.getLogger(YlServiceImpl.class);


    /**
     * 获取 各个供应商数据库中  支付表的数据   或  读取ftp上传的txt文件
     *    返回的Map中包含两个list
     *        yesList：对账成功的数据
     *        noList：precordId或plateNum对上，金额没有对上的数据
     *        noAllList：对账完全失败的数据
     * @param parkId  停车场id
     * @return
     * @throws Exception
     */
    public Map<String,Object> getStatementInfo(String parkId/*,String supplierId*/, String beginDate, String endDate) throws Exception{
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
            String responseMessage = UrlConnectUtil.getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?parkid="+parkId+"&starttime="+new_beginDate+"&deadline="+new_endDate);
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
        param.put("beginDate", beginDate/*+" 00:00:00"*/);
        param.put("endDate", endDate/*+" 23:59:59"*/);

        //获取停车场不需要对账的数据，进行排除
        List<Integer> precordidList = ylMapper.excludeNoReconciliation(param);
        Iterator<StatementAccount> siter = statementAccountList.iterator();
        while (siter.hasNext()) {
            StatementAccount  statementAccount= siter.next();
            if(statementAccount.getExplain().equals("微信支付宝支付")||statementAccount.getExplain().equals("预缴费")||statementAccount.getExplain().equals("现金收费")||statementAccount.getExplain().equals("预交费成功")||statementAccount.getExplain().equals("未知")||statementAccount.getExplain().equals("余额支付")) {
                siter.remove();
                continue;
            }
            boolean flag = false;
            int p = statementAccount.getpRecordId();
            for(int k = 0;k<precordidList.size();k++){
                if(precordidList.get(k)!=null&&!"".equals(precordidList.get(k))){
                    if(p==precordidList.get(k)){
                        flag = true;
                        break;
                    }
                }
            }
            if(flag){
                siter.remove();
            }
        }

        List<Map<String,Object>> kabinReconciliation  = ylMapper.getParkrecordInfo(param);   //卡宾支付信息  银联钱包、applePay、银联在线
        List<Map<String,Object>> kabinReconciliation_dk = ylMapper.getBindUnionPay(param);  //卡宾支付信息   银联代扣
        //把银联钱包、applePay、银联在线、银联代扣、银联权益的数据放在一个集合里进行对账
        for(int i = 0;i<kabinReconciliation_dk.size();i++){
            kabinReconciliation.add(kabinReconciliation_dk.get(i));
        }
        param.put("paytrench_paytype","1,3,4,5"); //银联代收   applyPay   银联在线   银联钱包
        List<Map<String,Object>> unionPayUploadData = commonMapper.getUnionPayUploadData(param); //支付渠道支付信息
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

        int a = 0,b = 0,c = 0,d=0,e=0;
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
                            d++;
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
                if(!(statementAccount.getExplain().equals("微信支付宝支付")||statementAccount.getExplain().equals("预缴费")||statementAccount.getExplain().equals("现金收费")||statementAccount.getExplain().equals("预交费成功")||statementAccount.getExplain().equals("未知")||statementAccount.getExplain().equals("余额支付"))){
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
			/*Date datetime = DateUtils.formatStrToDate1(temp.get("createtime").toString());
			if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(beginDate).getTime())==-1){  //该条记录的时间必须比开始时间要大
				continue;
			}
			if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(endDate).getTime())==1){  //该条记录的时间必须比结束时间要小
				continue;
			}*/
            cabin_originalAmountAll += Double.parseDouble(temp.get("originalAmount").toString());

            cabin_recordAmountAll += Double.parseDouble(temp.get("recordAmount").toString());

			/*Double recordAmountAll = 0d;
			if(temp.get("couponamount")!=null&&!temp.get("recordAmount").equals("")){
				cabin_recordAmountAll += Double.parseDouble(temp.get("recordAmount").toString());
			}else{
				cabin_recordAmountAll += recordAmountAll;
			}*/
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
                    filter_count++;
                    continue;
                }
                if(DateUtils.compare_date(datetime.getTime(), DateUtils.formatStrToDate1(endDate).getTime())==1){  //该条记录的时间必须比结束时间要小
                    filter_count++;
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
                            a++;
                        }else{
                            b++;
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
                            a++;
                            e++;
                        }else{
                            b++;
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
                c++;
            }
        }

        //停车场和一咻对账结果集合   和  支付渠道  进行对账
        Iterator<Map<String,Object>> iter = yesAllList.iterator();
        while (iter.hasNext()) {
            Map<String,Object> temp = iter.next();
            boolean flag = false;
            for(int j = 0;j<unionPayUploadData.size();j++){
                Map<String,Object> unionPay = unionPayUploadData.get(j);
                String yl_id = null;
                String cabin_id = null;
                String paytype = null;
                if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("dkid").toString();
                    paytype = "银联代收";
                }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                    yl_id = unionPay.get("UnionpayRightsId").toString();
                    paytype = "银联权益";
                }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "applyPay";
                }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "银联在线";
                }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                    yl_id = unionPay.get("pRecordId").toString();
                    cabin_id = temp.get("pRecordId").toString();
                    paytype = "银联钱包";
                }
                if(yl_id.equals(cabin_id)){
                    flag = true;
                    temp.put("yl_money", unionPay.get("money"));
                    temp.put("yl_id",yl_id);
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


        List<Map<String,Object>> yl_yx_yesList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> yl_yx_noList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> yl_yx_noAllList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> cabin_yl_yx_noAllList = new ArrayList<Map<String,Object>>();
        //以支付渠道为中心和一咻对账
        for(int i = 0;i<unionPayUploadData.size();i++){
            Map<String,Object> unionPay = unionPayUploadData.get(i);
            boolean flag = false;
            for(int j = 0;j<kabinReconciliation.size();j++){
                Map<String,Object> temp = kabinReconciliation.get(j);
                String yl_id = null;
                String cabin_id = null;
                String paytype = null;
                if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("dkid").toString();
                    paytype = "银联代收";
                }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                    yl_id = unionPay.get("UnionpayRightsId").toString();
                    paytype = "银联权益";
                }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "applyPay";
                }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "银联在线";
                }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                    yl_id = unionPay.get("pRecordId").toString();
                    cabin_id = temp.get("pRecordId").toString();
                    paytype = "银联钱包";
                }
                if(yl_id.equals(cabin_id)){
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
                    tempMap.put("yl_recordAmount",unionPay.get("money"));      //支付渠道支付金额
                    tempMap.put("yl_explain",paytype);         //支付渠道支付类型
                    if(Double.parseDouble(unionPay.get("money").toString())==Double.parseDouble(temp.get("recordAmount").toString())){
                        yl_yx_yesList.add(tempMap);
                    }else{
                        yl_yx_noList.add(tempMap);
                    }
                    break;
                }
            }
            if(!flag){
                String yl_id = null;
                String cabin_id = null;
                String paytype = null;
                if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    paytype = "银联代收";
                }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                    yl_id = unionPay.get("UnionpayRightsId").toString();
                    paytype = "银联权益";
                }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    paytype = "applyPay";
                }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    paytype = "银联在线";
                }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                    yl_id = unionPay.get("pRecordId").toString();
                    paytype = "银联钱包";
                }
                Map<String, Object> tempMap = new HashMap<String,Object>();
                tempMap.put("yl_recordAmount",unionPay.get("money"));      //支付渠道支付金额
                tempMap.put("yl_explain",paytype);         //支付渠道支付类型
                tempMap.put("yl_num",yl_id);
                yl_yx_noAllList.add(tempMap);
            }
        }


        //以一咻为中心和支付渠道进行对账
        for(int i = 0;i<kabinReconciliation.size();i++){
            Map<String,Object> temp = kabinReconciliation.get(i);
            if(temp.get("paytype").equals("UnionpayWallet")){  //如果是银联钱包的数据
                if(temp.get("recordStatus").equals("01")){
                    continue;
                }
            }
            boolean flag = false;
            for(int j = 0;j<unionPayUploadData.size();j++){
                Map<String,Object> unionPay = unionPayUploadData.get(j);
                String yl_id = null;
                String cabin_id = null;
                String paytype = null;
                if(unionPay.get("payType").equals("1")){  //1.银联代收    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("dkid").toString();
                    paytype = "银联代收";
                }else if(unionPay.get("payType").equals("2")){  //2.银联权益   UnionpayRightsId
                    yl_id = unionPay.get("UnionpayRightsId").toString();
                    paytype = "银联权益";
                }else if(unionPay.get("payType").equals("3")){  //3.applyPay   crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "applyPay";
                }else if(unionPay.get("payType").equals("4")){   //4.银联在线    crecordid
                    yl_id = unionPay.get("cRecordId").toString();
                    cabin_id = temp.get("cRecordId").toString();
                    paytype = "银联在线";
                }else if(unionPay.get("payType").equals("5")){   //5.银联钱包  precordid
                    yl_id = unionPay.get("pRecordId").toString();
                    cabin_id = temp.get("pRecordId").toString();
                    paytype = "银联钱包";
                }
                if(yl_id.equals(cabin_id)){
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

        rtnMap.put("yl_yx_yesList",yl_yx_yesList);
        rtnMap.put("yl_yx_noList",yl_yx_noList);
        rtnMap.put("yl_yx_noAllList",yl_yx_noAllList);
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

        System.out.println("支付渠道对账成功："+yl_yx_yesList.size());
        System.out.println("支付渠道不完全成功："+yl_yx_noList.size());
        System.out.println("支付渠道完全失败："+yl_yx_noAllList.size());
        System.out.println("卡宾-支付渠道对账完全失败："+cabin_yl_yx_noAllList.size());

        return rtnMap;

    }


    /**
     * 停车场对不上的数据，分析对不上的原因
     * @param park_noAll
     * @param param
     * @return
     */
    public List<Map<String,Object>> getParkNoAllRemark(List<Map<String,Object>> park_noAll,Map<String,Object> param){
        param.put("beginDate",DateUtils.format(DateUtils.formatStrToDate1(param.get("beginTime").toString()))+" 00:00:00");
        param.put("endDate",DateUtils.format(DateUtils.formatStrToDate1(param.get("endTime").toString()))+" 23:59:59");
        StringBuffer sb = new StringBuffer();
        //查看对账失败列表中哪些数据是招商代扣和银联权益数据,把招商代扣和银联权益的排除
        for(int i = 0;i<park_noAll.size();i++){
            Map<String,Object> tempData = park_noAll.get(i);
            if(tempData.get("pRecordId")!=null&&!"".equals(tempData.get("pRecordId"))){
                if(i<park_noAll.size()-1){
                    sb.append(tempData.get("pRecordId")).append(",");
                }else{
                    sb.append(tempData.get("pRecordId"));
                }
            }
        }
        //过滤招商代扣和银联权益数据
        List<Map<String,Object>> EquityPlatPayOrCmbAutoPayInfo = commonMapper.getEquityPlatPayOrCmbAutoPayInfo(sb.toString());
        Iterator<Map<String,Object>> iter = park_noAll.iterator();
        while (iter.hasNext()) {
            Map<String,Object> tempData = iter.next();
            boolean flag = false;
            if(tempData.get("pRecordId")!=null&&!"".equals(tempData.get("pRecordId"))){
                for(int j = 0;j<EquityPlatPayOrCmbAutoPayInfo.size();j++){
                    Map<String,Object> EquityPlatPayOrCmbAuto = EquityPlatPayOrCmbAutoPayInfo.get(j);
                    if(tempData.get("pRecordId").equals(EquityPlatPayOrCmbAuto.get("pRecordId"))){
                        if(EquityPlatPayOrCmbAuto.get("paytype").equals("EquityPlatPay")||EquityPlatPayOrCmbAuto.get("paytype").equals("CmbAutoPay")){ //招商代扣和银联权益
                            flag = true;
                        }
                    }
                }
            }
            if(flag){
                iter.remove();
            }
        }

        //对不上得数据进行对比,找出是为什么对不上的原因
        for(int i = 0;i<park_noAll.size();i++){
            Map<String,Object> tempData = park_noAll.get(i);
            param.put("precordid",tempData.get("pRecordId"));
            Map<String,Object> tempMap = ylMapper.getDisagreeData_dk(param);
            if(tempMap!=null){  //    代扣失败  或   一咻存放的数据和停车场存放的数据 保存时间不一样  而选择时间区间时恰好把一咻的数据给过滤了
                if(tempMap.get("is_succ_pay").toString().equals("false")||Boolean.parseBoolean(tempMap.get("is_succ_pay").toString())==false){  //代扣失败
                    tempData.put("remark","代扣失败时,停车场有记录,但一咻数据已经过滤,【一咻数据库说明:"+tempMap.get("query_remark")+"】");
                }else{  //代扣成功
                    //如：
                    tempData.put("remark","一咻生产数据时间和停车场生产数据时间不一致,如：选择时间为2018-01-01 15:00:00至2018-01-01 23:00:00,停车场时间存的是2018-01-01 15:00:12,一咻存放的是2018-01-01 14:59:58，这种情况导致一咻数据被过滤!");
                }
            }else{
                tempMap = ylMapper.getDisagreeData_zzf_yhj(param);
                if(tempMap!=null){  //有precordid
                    if(tempMap.get("paytype").equals("CouponPay")){ //优惠券形式的数据,此形式不用进行对账
                        tempData.put("remark","优惠券支付,停车场数据存放的是【扣费缴费成功】,与【代扣】存的支付方式是一样的,导致无法过滤,数据对不上");
                    }
                }else{  //没有precordid
                    Map<String,Object> tempParam = new HashMap<String,Object>();
                    tempParam.put("plateNum",tempData.get("plateNum"));
                    tempParam.put("park_originalAmount",tempData.get("park_originalAmount"));
                    tempParam.put("park_recordAmount",tempData.get("park_recordAmount"));
                    tempParam.put("beginDate",param.get("beginDate"));
                    tempParam.put("endDate",param.get("endDate"));
                    tempMap = ylMapper.getDisagreeData_zzf_weixinzhifubao(tempParam);
                    if(tempMap!=null){
                        if(tempMap.get("paytype").equals("YiXiuWxPay")){
                            tempData.put("remark","一咻微信自助缴费,此支付方式走银商对账,停车场数据存放的是【扣费缴费成功】,与【代扣】存的支付方式是一样的,导致无法过滤,数据对不上");
                        }else{
                            tempData.put("remark","数据没找到,可能车牌号识别有误");
                        }
                    }else{
                        tempData.put("remark","数据没找到,可能车牌号识别有误");
                    }
                }
            }
        }
        for(int i = 0;i<park_noAll.size();i++){
            System.out.println(park_noAll.get(i));
        }
        return park_noAll;
    }
}
