package com.reconciliation.controller;

import com.reconciliation.service.CommonService;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.FileUpload;
import com.reconciliation.util.ObjectExcelRead;
import com.reconciliation.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/14.
 * 银联钱包
 */
@Controller
public class UnionpayWallet {


    @Autowired
    private CommonService commonService;

    @RequestMapping(value = "/goUploadExcelView")
    public String goUploadView(){
        return "upload";
    }

    /**
     * 上传银联钱包excel
     * @param file
     * @throws ParseException
     * @throws NumberFormatException
     */
    @PostMapping(value = "/uploadExcel")
    public String  uploadExcel(@RequestParam(value="excel",required=false) MultipartFile file, Model model) {
        try{
            if (null != file && !file.isEmpty()) {
                String filePath = PathUtil.getClasspath() + "uploadFiles/";								//文件上传路径
                String fileName = FileUpload.fileUp(file, filePath, "unionPayWallet");							//执行上传
                List<Map<String,Object>> listPd = (List) ObjectExcelRead.readExcel(filePath, fileName,1, 0, 0);		//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet
                Date date = new Date();
                String billDate = DateUtils.format(DateUtils.addOneDay(new Date(),-1));
                for(int i=0;i<listPd.size();i++){
                    Map<String,Object> pd = new HashMap<String,Object>();
                    pd.put("money", listPd.get(i).get("var1"));
                    pd.put("cRecordId", "");
                    pd.put("pRecordId", listPd.get(i).get("var2"));
                    pd.put("UnionpayRightsId", "");
                    pd.put("parkId", listPd.get(i).get("var5"));
                    pd.put("createTime", date);
                    pd.put("payType", 5);
                    pd.put("billDate", billDate);
                    commonService.insertUnionpay(pd);
                }
            }
            model.addAttribute("msg",DateUtils.format(DateUtils.addOneDay(new Date(),-1))+"银联钱包数据上传成功");
        }catch (Exception e){
            model.addAttribute("msg",DateUtils.format(DateUtils.addOneDay(new Date(),-1))+"银联钱包数据上传失败");
        }
        return "upload";
    }


}
