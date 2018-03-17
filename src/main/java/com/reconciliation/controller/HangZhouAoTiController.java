package com.reconciliation.controller;

import com.reconciliation.service.HangZhouAoTiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */
@Controller
public class HangZhouAoTiController {

    @Autowired
    private HangZhouAoTiService hangZhouAoTiService;


    /**
     * 去杭州奥体支付信息页面
     * @return
     */
    @RequestMapping(value = "goHangZhouAoTiView")
    public String goHangZhouAoTiView(){
        return "hangzhouaoti/hangzhouaotiPay";
    }

    /**
     * 获取杭州奥体支付信息
     * @return
     */
    @RequestMapping(value = "/getHangZhouAoTiData")
    @ResponseBody
    public List<Map<String ,Object>> getHangZhouAoTiPayInfo(HttpServletRequest request){
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("beginTime",request.getParameter("beginTime"));
        param.put("endTime",request.getParameter("endTime"));
        param.put("platenum",request.getParameter("platenum").toString().trim());
        return hangZhouAoTiService.getHangZhouAoTiPayInfo(param);
    }

}
