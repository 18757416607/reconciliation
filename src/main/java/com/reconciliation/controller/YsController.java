package com.reconciliation.controller;

import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.CommonService;
import com.reconciliation.service.YsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 * 银商对账
 */
@Controller
public class YsController {

    @Autowired
    private CommonService commonService;
    @Autowired
    private YsService ysService;

    /**
     * 银商需要一咻提供停车场费用信息
     *   去停车场费用查看页面
     * @return
     */
    @RequestMapping(value = "/ysParkPayInfo")
    public String ysParkPayInfo(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "ysview/parkPayInfo";
    }

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
    @RequestMapping(value =  "/getParkPayInfo")
    @ResponseBody
    public List<StatementAccount> getParkPayInfo(String parkid,String beginDate,String endDate,Model model){
        Map<String,Object> map = ysService.getParkPayInfo(parkid,beginDate,endDate);
        List<StatementAccount> statementAccountList = (List<StatementAccount>)map.get("list");
        return statementAccountList;
    }


}
