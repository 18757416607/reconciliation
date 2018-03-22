package com.reconciliation.service.impl;

import com.reconciliation.dao.CommonMapper;
import com.reconciliation.pojo.Config;
import com.reconciliation.service.CommonService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUtil;
import com.reconciliation.util.acp.sdk.DemoBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
@Service
public class CommonServiceImpl implements CommonService{

    @Autowired
    private Config config;
    @Autowired
    private CommonMapper commonMapper;

    /**
     *  数据从银联上获取对账单信息  保存在一咻数据库
     *  1.银联代收
     *  2.主动支付->  applePay
     *  3.主动支付->  银联在线
     *  4.银联权益
     * @throws Exception
     */
    @Transactional
    public void writeUnionPay() throws Exception{
        String[] commercialCodeArr = config.getCommercialCode().split(",");
        String dateStr = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.getCurrDateStr()),-1))).substring(2,8); //"171225";
        Date date = new Date();
        for(int j = 0;j<commercialCodeArr.length;j++){
            String path = config.getAccount_filePath() + config.getAccount_filePath_prefix() + dateStr + config.getAccount_filePath_suffix() + commercialCodeArr[j];
            List<Map> list = DemoBase.parseZMFile(path);
            for(int i = 0;i<list.size();i++){
                Map<String,Object> temp = new HashMap<String,Object>();
                String id = list.get(i).get(11).toString().trim();   //商户订单号
                Double money = Double.parseDouble(list.get(i).get(6).toString().trim())/100;  //交易金额
                if(j==0){ //银联代收
                    temp.put("cRecordId", id);
                    temp.put("pRecordId", "");
                    temp.put("UnionpayRightsId", "");
                    temp.put("payType", 1);
                }else if(j==1){  //银联权益
                    temp.put("cRecordId", "");
                    temp.put("pRecordId", "");
                    temp.put("UnionpayRightsId", id);
                    temp.put("payType", 2);
                }else if(j==2){  //applyPay
                    temp.put("cRecordId", id);
                    temp.put("pRecordId", "");
                    temp.put("UnionpayRightsId", "");
                    temp.put("payType", 3);
                }else if(j==3){  //银联在线
                    temp.put("cRecordId", id);
                    temp.put("pRecordId", "");
                    temp.put("UnionpayRightsId", "");
                    temp.put("payType", 4);
                }
                temp.put("money",money);
                if(commercialCodeArr[j].equals("898330275230083")){  //银联代扣

						/*String str = list.get(i).get(34).toString();
						JSONObject object = JSONObject.parseObject(str.substring(0,str.indexOf(","))+"}");
						String[] objArr = object.get("parkId").toString().split("&");
						if(objArr.length>1){  //说明这条记录是补缴的

						}else{

						}*/

                    temp.put("parkId", list.get(i).get(34).toString().substring(11,15));
                }else if(commercialCodeArr[j].equals("898330275230081")||commercialCodeArr[j].equals("898330275230082")){  //ApplyPay或银联在线
                    temp.put("parkId", list.get(i).get(34).toString().substring(0,4));
                }
                temp.put("createTime", date);
                String bill = DateUtils.getYear()+list.get(i).get(4).toString();//交易传输时间
                bill = DateUtils.convertDateFormat(bill);
                temp.put("billDate", bill);
                temp.put("cmbId","");
                commonMapper.insertUnionpay(temp);
				/*}*/
            }
        }

        //招商数据
        /*FileUtil fileUtil = new FileUtil();
        List<Map<String,Object>> cmdList = fileUtil.getCmbData(config);
        for(int i = 0;i<cmdList.size();i++){
            commonMapper.insertUnionpay(cmdList.get(i));
        }*/
    }


    /**
     * 查询所有的停车场信息
     * @return
     */
    public List<Map<String,Object>> getParkList(){
        return commonMapper.getParkList();
    }

    /**
     * 添加  银联  对账单数据
     * @param param
     * @return
     */
    public int insertUnionpay(Map<String,Object> param){
        return commonMapper.insertUnionpay(param);
    }


}
