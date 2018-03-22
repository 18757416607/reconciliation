package com.reconciliation.controller;

import com.reconciliation.pojo.Config;
import com.reconciliation.service.CommonService;
import com.reconciliation.service.YlService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUpload;
import com.reconciliation.util.ObjectExcelRead;
import com.reconciliation.util.PathUtil;
import com.reconciliation.util.acp.sdk.AcpService;
import com.reconciliation.util.acp.sdk.DemoBase;
import com.reconciliation.util.acp.sdk.LogUtil;
import com.reconciliation.util.acp.sdk.SDKConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Administrator on 2018/3/13.
 * 银联对账
 */
@Controller
public class YlController {

    @Autowired
    private CommonService commonService;
    @Autowired
    private YlService ylService;
    @Autowired
    private Config config;


    /**
     * 去对账单首页
     * @return
     */
    @RequestMapping(value = "/goStatement",method = RequestMethod.GET)
    public String goStatement(){
        return "duizhangdan/reconciliation-index";
    }
    @RequestMapping(value = "/yl_1")
    public String yl_1(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-1";
    }
    @RequestMapping(value = "/yl_2")
    public String yl_2(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-2";
    }
    @RequestMapping(value = "/yl_3")
    public String yl_3(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-3";
    }
    @RequestMapping(value = "/yl_4")
    public String yl_4(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-4";
    }
    @RequestMapping(value = "/yl_5")
    public String yl_5(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-5";
    }
    @RequestMapping(value = "/yl_6")
    public String yl_6(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-6";
    }
    @RequestMapping(value = "/yl_7")
    public String yl_7(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-yl-7";
    }

    /**
     * 获取三方对账成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getReconciliationYesAllData")
    @ResponseBody
    public List<Map<String,Object>> getReconciliationYesAllData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("yesAllList");
    }

    /**
     * 停车场、一咻对账成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuParkData")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuParkData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("yesList");
    }

    /**
     * 停车场、一咻对账不完全成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoParkData")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoParkData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("noList");
    }

    /**
     * 停车场、一咻对账失败数据
     * 		一咻端有数据，停车场端没有数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoAllParkData1")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoAllParkData1(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("cabin_noAllList");
    }
    /**
     * 停车场、一咻对账失败数据
     * 		停车场端有数据，一咻端没有数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoAllParkData2")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoAllParkData2(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("park_noAllList");
        if(list.size()>0){
            return  ylService.getParkNoAllRemark(list,param);
        }
        return new ArrayList<>();
    }


    /**
     * 支付渠道  一咻  对账成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuYesData")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuYesData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("yl_yx_yesList");
        return list;
    }

    /**
     * 支付渠道  一咻  对账不完全成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuNoData")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("yl_yx_noList");
        return list;
    }


    private List<Map<String,Object>> cabin_yl_yx_noAllList;
    /**
     * 支付渠道  一咻  对账失败数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuNoAllData")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoAllData(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = ylService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("yl_yx_noAllList");
        cabin_yl_yx_noAllList = (List<Map<String,Object>>)map.get("cabin_yl_yx_noAllList");
        return list;
    }

    /**
     * 支付渠道  一咻  对账失败数据
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getPayYiXiuNoAllData1")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoAllData1(HttpServletRequest request) throws Exception{
        return cabin_yl_yx_noAllList;
    }


}
