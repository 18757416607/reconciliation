package com.reconciliation.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

/**
 *
 * UrlConnection Util
 *
 * */
public class UrlConnectUtil {

    /*
     * get HTTPS
     * */
    public static void getHttps(String url)throws Exception{
        URL requestURI=new URL(url);
        HttpsURLConnection conn=(HttpsURLConnection)requestURI.openConnection();//如果是https的请求则是HttpsURLConnection，如果是http则是HttpURLConnection
        conn.setRequestMethod("GET");//设置请求方式
        conn.setDoInput(true);//打开input流
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type","text/html");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        InputStream input=null;
        BufferedReader reader=null;
        StringBuffer result=new StringBuffer("");

        input=conn.getInputStream();
        reader=new BufferedReader(new InputStreamReader(input));
        String temp=null;
        while((temp=reader.readLine())!=null){
            result.append(temp);
        }
        reader.close();
        input.close();
        System.out.println(result);

    }

    /*
     * get HTTP
     * */
    public static String getHttp(String url,String accessToken)throws Exception{
        URL requestURI=new URL(url);
        HttpURLConnection conn=(HttpURLConnection)requestURI.openConnection();//如果是https的请求则是HttpsURLConnection，如果是http则是HttpURLConnection
        conn.setRequestMethod("GET");//设置请求方
        conn.setDoInput(true);//打开input流
        if(accessToken != ""){
            conn.setRequestProperty("accessToken",accessToken);
            System.out.println("+++++++asstoken");
        }
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type","text/html");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        InputStream input=null;
        BufferedReader reader=null;
        StringBuffer result=new StringBuffer("");

        input=conn.getInputStream();
        reader=new BufferedReader(new InputStreamReader(input));
        String temp=null;
        while((temp=reader.readLine())!=null){
            result.append(temp);
        }
        reader.close();
        input.close();
        return result.toString();

    }

    public static String getHttpJson(String url)throws Exception{
        URL requestURI=new URL(url);
        HttpURLConnection conn=(HttpURLConnection)requestURI.openConnection();//如果是https的请求则是HttpsURLConnection，如果是http则是HttpURLConnection
        conn.setRequestMethod("POST");//设置请求方
        conn.setDoInput(true);//打开input流
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        InputStream input=null;
        BufferedReader reader=null;
        StringBuffer result=new StringBuffer("");
        input=conn.getInputStream();
        reader=new BufferedReader(new InputStreamReader(input,"UTF-8"));
        String temp=null;
        while((temp=reader.readLine())!=null){
            result.append(temp);
        }
        reader.close();
        input.close();
        return result.toString();

    }


    /*
     * post requset.getparam...
     * */
    public static void post(String urlstr,String param) throws Exception{
        URL url=new URL(urlstr);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        StringBuffer params=new StringBuffer();
        //"userName=user123&password=123456"
        params.append(param);
        byte[] bytes=params.toString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input));
        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        System.out.println(result);
    }


    public static String postRtnStr(String urlstr,String param) throws Exception{
        URL url=new URL(urlstr);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        StringBuffer params=new StringBuffer();
        //"userName=user123&password=123456"
        params.append(param);
        byte[] bytes=params.toString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input));
        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }

    public static String post(String urlstr,Map<String, Object> param) throws Exception{
        URL url=new URL(urlstr);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
//        StringBuffer params=new StringBuffer();
//        for (Map.Entry<String, Object> entry : param.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//            params.append(entry.getKey()+"="+entry.getValue()+"&");
//        }
//        params.substring(0, params.length()-1);
        JSONObject object = (JSONObject) JSON.toJSON(param);
        System.out.println("发送数据为：" + object);
        byte[] bytes=object.toJSONString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"utf-8"));

        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }

    public static String postWithToken(String urlstr,Map<String, Object> param,String token) throws Exception{
        URL url=new URL(urlstr);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("accessToken", token);
        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
//        StringBuffer params=new StringBuffer();
//        for (Map.Entry<String, Object> entry : param.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//            params.append(entry.getKey()+"="+entry.getValue()+"&");
//        }
//        params.substring(0, params.length()-1);
        JSONObject object = (JSONObject) JSON.toJSON(param);
        System.out.println("发送数据为：" + object);
        byte[] bytes=object.toJSONString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"utf-8"));

        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }

    public static String httpsPost(String urlstr,Map<String, Object> param) throws Exception{
        URL url=new URL(urlstr);
        HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
//        StringBuffer params=new StringBuffer();
//        for (Map.Entry<String, Object> entry : param.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//            params.append(entry.getKey()+"="+entry.getValue()+"&");
//        }
//        params.substring(0, params.length()-1);
        JSONObject object = (JSONObject) JSON.toJSON(param);
        System.out.println("发送信息为:" + object.toJSONString());
        byte[] bytes=object.toJSONString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"utf-8"));

        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }


    public static String httpsPost(String urlstr,String param) throws Exception{
        URL url=new URL(urlstr);
        HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
//        StringBuffer params=new StringBuffer();
//        for (Map.Entry<String, Object> entry : param.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//            params.append(entry.getKey()+"="+entry.getValue()+"&");
//        }
//        params.substring(0, params.length()-1);
        byte[] bytes=param.getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"utf-8"));

        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }

    /**
     * 富士专用post请求,富士需要吧sign,appid,10位的时间戳放到请求头里
     */
    public static String httpPostFushi(String urlstr,Map<String, Object> param,String appid,String sign,String timestamp) throws Exception{
        URL url=new URL(urlstr);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);//上传参数，需要打开output流，将参数写到流中提交
        conn.setRequestProperty("Connection","keep-alive");
        conn.setRequestProperty("charset", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36");
        conn.setRequestProperty("sign",sign);
        conn.setRequestProperty("appid",appid);
        conn.setRequestProperty("timestamp",timestamp);
        conn.setRequestProperty("encry", "123456");
        JSONObject object = (JSONObject) JSON.toJSON(param);
        System.out.println("发送信息为:" + object.toJSONString());
        byte[] bytes=object.toJSONString().getBytes();
        conn.getOutputStream().write(bytes);

        InputStream input=conn.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"utf-8"));

        String result="";
        String temp="";
        while((temp=reader.readLine())!=null){
            result+=temp;
        }
        return result;
    }

    public static void main(String[] args) throws Exception{
	    /*Map<String ,Object> map = new HashMap<String, Object>();
	    map.put("key","024803aa7d3847d2a818cd660879007d");
        String result = httpsPost("http://localhost:8080/cabin_CMS/addUser.do",map);
        System.out.println(result);*/
        String a = getHttpJson("http://api.cabin-app.com:8000/carReconciliation/?parkid=1071&starttime=2017-12-25%2000:00:00&deadline=2017-12-25%2023:59:59");
        System.out.println(a);
    }
}
