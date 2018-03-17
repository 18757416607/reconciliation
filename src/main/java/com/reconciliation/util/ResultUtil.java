package com.reconciliation.util;


import com.reconciliation.pojo.Result;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/5.
 */
public class ResultUtil {

    public static String REQUESTFAILD = "request faild";  //请求失败


    /**
     * 请求成功
     * @param data
     * @return
     */
    public static Result requestSuccess(String data) throws Exception{
        Result result = new Result();
        result.setCode("00");
        result.setMsg("request success");
        if(data==null||data.equals("")){
            result.setData("");
        }else{
            //RSA加密
            /*result.setData(splitEncrypt(data));*/
            data = URLEncoder.encode(data,"UTF-8");
            result.setData(GZIPUtils.compress(data));
        }
        return  result;
    }


    /**
     * 请求失败
     * @return
     */
    public static  Result requestFaild(String msg){
        Result result = new Result();
        result.setCode("-1");
        if(msg==null){
            result.setMsg(REQUESTFAILD);
        }else{
            result.setMsg(msg);
        }
        return  result;
    }

    /**
     *  RSA加密最长为117字节
     *   处理：55个字符就分割成一个字符串进行加密,加密结果组装成一个List集合,前端进行解密组装
     * @param msg
     * @return
     */
    public static List<String> splitEncrypt(String msg) throws Exception{
        List<String> ncryptList = new ArrayList<String>();
        if(msg.length()<=55){
            ncryptList.add(SecurityUtils.encrypt(msg));
        }else{
            int size = msg.length()/55+1; //需要加密的次数
            int startNum = 0; //开始截取的索引
            int endNum = 55;   //结束截取的索引
            for(int i = 0;i < size; i++){
                if(i==size-1){
                    String leng = msg.substring(startNum,msg.length());
                    if(leng.length()==0){
                        break;
                    }
                    ncryptList.add(SecurityUtils.encrypt(msg.substring(startNum,msg.length())));
                }else{
                    ncryptList.add(SecurityUtils.encrypt(msg.substring(startNum,endNum)));
                }
                startNum+=55;
                endNum+=55;
            }
        }
        return ncryptList;
    }


}
