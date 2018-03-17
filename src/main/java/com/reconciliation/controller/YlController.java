package com.reconciliation.controller;

import com.reconciliation.pojo.Config;
import com.reconciliation.service.CommonService;
import com.reconciliation.service.YlService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.acp.sdk.AcpService;
import com.reconciliation.util.acp.sdk.DemoBase;
import com.reconciliation.util.acp.sdk.LogUtil;
import com.reconciliation.util.acp.sdk.SDKConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    /**
     * 获取  银联 的对账 文件
     *    只能一天一天获取对账文件 st
     * @param req
     * @param resp
     * @throws Exception
     */
    @RequestMapping(value = "/getUnionpayFile")
    public void getUnionpayFile(HttpServletRequest req,HttpServletResponse resp) throws Exception{
        SDKConfig.getConfig().loadPropertiesFromSrc();
        String settleDate = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.getCurrDateStr()),-1))).substring(4,8);//"1225";
        //String settleDate = req.getParameter("settleDate");
        String commercialCode = config.getCommercialCode();//银联全渠道商户号
        String signCert = config.getSignCert();//银联密钥路径
        String[] commercialCodeArr = commercialCode.split(",");
        String[] signCertArr = signCert.split(",");

        for(int i = 0;i<signCertArr.length;i++){
            Map<String, String> data = new HashMap<String, String>();

            /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
            data.put("version", DemoBase.version);               //版本号 全渠道默认值
            data.put("encoding", DemoBase.encoding);             //字符集编码 可以使用UTF-8,GBK两种方式
            data.put("signMethod", SDKConfig.getConfig().getSignMethod()); //签名方法
            data.put("txnType", "76");                           //交易类型 76-对账文件下载
            data.put("txnSubType", "01");                        //交易子类型 01-对账文件下载
            data.put("bizType", "000000");                       //业务类型，固定

            /***商户接入参数***/
            data.put("accessType", "0");                         //接入类型，商户接入填0，不需修改
            data.put("merId", commercialCodeArr[i]);              //商户代码，请替换正式商户号测试，如使用的是自助化平台注册的777开头的商户号，该商户号没有权限测文件下载接口的，请使用测试参数里写的文件下载的商户号和日期测。如需777商户号的真实交易的对账文件，请使用自助化平台下载文件。
            data.put("settleDate", settleDate);                  //清算日期，如果使用正式商户号测试则要修改成自己想要获取对账文件的日期， 测试环境如果使用700000000000001商户号则固定填写0119
            data.put("txnTime",DemoBase.getCurrentTime());       //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
            data.put("fileType", "00");                          //文件类型，一般商户填写00即可

            /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/
            //Map<String, String> reqData = AcpService.sign(data,DemoBase.encoding);				//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
            Map<String, String> reqData = AcpService.signByCertInfo(data,signCertArr[i], "879576", "utf-8");
            String url = SDKConfig.getConfig().getFileTransUrl();										//获取请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.fileTransUrl
            Map<String, String> rspData = AcpService.post(reqData, url,DemoBase.encoding);


            /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/

            //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
            String fileContentDispaly = "";
            if(!rspData.isEmpty()){
                if(AcpService.validate(rspData, DemoBase.encoding)){
                    LogUtil.writeLog("验证签名成功");
                    String respCode = rspData.get("respCode");
                    if("00".equals(respCode)){
                        String outPutDirectory =config.getDrive_path();
                        // 交易成功，解析返回报文中的fileContent并落地
                        String zipFilePath = AcpService.deCodeFileContent(rspData,outPutDirectory,DemoBase.encoding);
                        //对落地的zip文件解压缩并解析
                        List<String> fileList = DemoBase.unzip(zipFilePath, outPutDirectory);
                        //解析ZM，ZME文件
                        fileContentDispaly ="<br>获取到商户对账文件，并落地到"+outPutDirectory+",并解压缩 <br>";
                        for(String file : fileList){
                            if(file.indexOf("ZM_")!=-1){
                                List<Map> ZmDataList = DemoBase.parseZMFile(file);
                                fileContentDispaly = fileContentDispaly+DemoBase.getFileContentTable(ZmDataList,file);
                            }else if(file.indexOf("ZME_")!=-1){
                                DemoBase.parseZMEFile(file);
                            }
                        }
                        //TODO
                    }else{
                        //其他应答码为失败请排查原因
                        //TODO
                    }
                }else{
                    LogUtil.writeErrorLog("验证签名失败");
                    //TODO 检查验证签名失败的原因
                }
            }else{
                //未返回正确的http状态
                LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
            }

            String reqMessage = DemoBase.genHtmlResult(reqData);
            String rspMessage = DemoBase.genHtmlResult(rspData);
            resp.getWriter().write("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage + fileContentDispaly);
        }
    }



    /**
     *  数据从银联上获取对账单信息  保存在一咻数据库
     *  1.银联代收
     *  2.主动支付->  applePay
     *  3.主动支付->  银联在线
     *  4.银联权益
     * @throws Exception
     */
    @RequestMapping(value = "/writeUnionPay")
    public void writeUnionPay() throws Exception{
        commonService.writeUnionPay();
        String drive_path = config.getAccount_filePath();//盘符
        String[] commercialCodeArr = config.getCommercialCode().split(",");//商户号
        String account_filePath_prefix = config.getAccount_filePath_prefix();//前缀
        String account_filePath_suffix = config.getAccount_filePath_suffix();//后缀
        String dataDate = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.getCurrDateStr()),-1))).substring(2,8);
        String zipDate = DateUtils.formatDate(DateUtils.format(DateUtils.addOneDay(DateUtils.format(DateUtils.getCurrDateStr()),-1)));
		/*for(int i = 0;i<commercialCodeArr.length;i++){ //循环删除银联对账文件
			StringBuffer filename = new StringBuffer(account_filePath_prefix).append(dataDate).append(account_filePath_suffix).append(commercialCodeArr[i]);
			StringBuffer filename1 = new StringBuffer(commercialCodeArr[i]).append("_").append(zipDate).append(".zip");
			StringBuffer filename2 = new StringBuffer("RD2010").append(dataDate).append("01_").append(commercialCodeArr[i]);
			FileUtil.delFile(drive_path, filename.toString());
			FileUtil.delFile(drive_path, filename1.toString());
			FileUtil.delFile(drive_path, filename2.toString());
		}*/
    }

}
