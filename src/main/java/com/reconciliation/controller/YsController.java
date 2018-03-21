package com.reconciliation.controller;

import com.alibaba.fastjson.JSON;
import com.reconciliation.pojo.Result;
import com.reconciliation.pojo.StatementAccount;
import com.reconciliation.service.CommonService;
import com.reconciliation.service.YsService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
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
     * 登陆页面
     * @return
     */
    @RequestMapping(value = "/login")
    public String login(){
        return "ysview/login";
    }

    @RequestMapping(value = "/loginCheck")
    @ResponseBody
    public String loginCheck(){
        return "00";
    }

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

    double originalAmount = 0; //应付金额
    double recordAmount = 0; //实付金额
    double couponamount = 0; //优惠金额


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
    public List<StatementAccount> getParkPayInfo(String parkid,String beginDate,String endDate,Model model) throws  Exception{
        Map<String,Object> map = ysService.getParkPayInfo(parkid,beginDate,endDate);
        originalAmount = Double.parseDouble(map.get("originalAmount").toString());
        recordAmount = Double.parseDouble(map.get("recordAmount").toString());
        couponamount = Double.parseDouble(map.get("couponamount").toString());
        return (List<StatementAccount>)map.get("list");
    }

    /**
     * 获取停车场费用总计
     * @return
     */
    @PostMapping(value = "/getTotalCost")
    @ResponseBody
    public Map<String,Double> getTotalCost(){
        Map<String,Double> map = new HashMap<String,Double>();
        map.put("originalAmount",originalAmount);
        map.put("recordAmount",recordAmount);
        map.put("couponamount",couponamount);
        return map;
    }


    /**
     * 停车场详细信息导出
     * @param parkid  停车场ID
     * @param beginDate  开始时间
     * @param endDate    结束时间
     * @param request
     * @param response
     */
    @RequestMapping(value = "/downParkDetailExcel")
    public void downParkDetailExcel(String parkid, String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
         ysService.downParkDetailExcel(parkid,beginDate,endDate,request,response);
    }


    /**
     * 所有停车场信息导出
     * @param beginDate  开始时间
     * @param endDate    结束时间
     * @param request
     * @param response
     */
    @RequestMapping(value = "/downParkAllExcel")
    public void downParkAllExcel(String beginDate, String endDate, HttpServletRequest request, HttpServletResponse response){
        ysService.downParkAllExcel(beginDate,endDate,request,response);
    }

}
