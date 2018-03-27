package com.reconciliation.controller;

import com.reconciliation.service.CmbService;
import com.reconciliation.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 * 招商对账
 */
@Controller
public class CmbController {

    @Autowired
    private CommonService commonService;
    @Autowired
    private CmbService cmbService;


    @RequestMapping(value = "/zs_1")
    public String zs_1(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-1";
    }
    @RequestMapping(value = "/zs_2")
    public String zs_2(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-2";
    }
    @RequestMapping(value = "/zs_3")
    public String zs_3(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-3";
    }
    @RequestMapping(value = "/zs_4")
    public String zs_4(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-4";
    }
    @RequestMapping(value = "/zs_5")
    public String zs_5(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-5";
    }
    @RequestMapping(value = "/zs_6")
    public String zs_6(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-6";
    }
    @RequestMapping(value = "/zs_7")
    public String zs_7(Model model){
        model.addAttribute("parkList",commonService.getParkList());
        return "duizhangdan/reconciliation-zs-7";
    }


    /**
     * 获取三方对账成功
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getReconciliationYesAllData_zs")
    @ResponseBody
    public List<Map<String,Object>> getReconciliationYesAllData_zs(HttpServletRequest request) throws  Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = cmbService.getStatementInfo(parkId,beginTime,endTime);
        return (List<Map<String,Object>>)map.get("yesAllList");
    }

    /**
     * 停车场、一咻对账成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuParkData_zs")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuParkData_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("yesList");
    }

    /**
     * 停车场、一咻对账不完全成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoParkData_zs")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoParkData_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("noList");
    }

    /**
     * 停车场、一咻对账失败数据
     * 		一咻端有数据，停车场端没有数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoAllParkData1_zs")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoAllParkData1_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        return (List<Map<String,Object>>)map.get("cabin_noAllList");
    }

    /**
     * 停车场、一咻对账失败数据
     * 		停车场端有数据，一咻端没有数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getYiXiuNoAllParkData2_zs")
    @ResponseBody
    public List<Map<String,Object>> getYiXiuNoAllParkData2_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("park_noAllList");
        if(list.size()>0){
            return  null;//commonStatementService.getParkNoAllRemark(list,param);
        }else{
            return new ArrayList<>();
        }

    }

    /**
     * 支付渠道  一咻  对账成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuYesData_zs")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuYesData_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("zs_yx_yesList");
        return list;
    }

    /**
     * 支付渠道  一咻  对账不完全成功数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuNoData_zs")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoData_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("zs_yx_noList");
        return list;
    }


    private List<Map<String,Object>> cabin_yl_yx_noAllList;
    /**
     * 支付渠道  一咻  对账失败数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getPayYiXiuNoAllData_zs")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoAllData_zs(HttpServletRequest request) throws Exception{
        String parkId = request.getParameter("parkId");
        String beginTime = request.getParameter("beginTime");
        String endTime = request.getParameter("endTime");
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("parkId",parkId);
        param.put("beginTime",beginTime);
        param.put("endTime",endTime);
        Map<String,Object> map = cmbService.getStatementInfo(parkId, beginTime, endTime);
        List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("zs_yx_noAllList");
        cabin_yl_yx_noAllList = (List<Map<String,Object>>)map.get("cabin_yl_yx_noAllList");
        return list;
    }

    /**
     * 支付渠道  一咻  对账失败数据
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getPayYiXiuNoAllData1_zs")
    @ResponseBody
    public List<Map<String,Object>> getPayYiXiuNoAllData1_zs(HttpServletRequest request) throws Exception{
        return cabin_yl_yx_noAllList;
    }


}
